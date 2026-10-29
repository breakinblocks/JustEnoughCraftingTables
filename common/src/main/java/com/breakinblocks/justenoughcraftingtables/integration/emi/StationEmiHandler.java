package com.breakinblocks.justenoughcraftingtables.integration.emi;

import com.breakinblocks.justenoughcraftingtables.integration.RecipeFill;
import com.breakinblocks.justenoughcraftingtables.menu.ItemKey;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StationEmiHandler implements EmiRecipeHandler<StationMenu> {
    private static final int MISSING_COLOR = 0x44FF0000;

    @Nullable
    private RecipeFill.Pool cachedPool;

    @Nullable
    private EmiPlayerInventory cachedInventory;

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<StationMenu> screen) {
        RecipeFill.Pool pool = RecipeFill.pool(screen.getMenu());
        if (cachedInventory == null || cachedPool != pool) {
            List<EmiStack> stacks = new ArrayList<>(pool.counts().size());
            for (Map.Entry<ItemKey, Long> entry : pool.counts().entrySet()) {
                stacks.add(EmiStack.of(entry.getKey().stack(), entry.getValue()));
            }
            cachedInventory = new EmiPlayerInventory(stacks);
            cachedPool = pool;
        }
        return cachedInventory;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING
                && recipe.supportsRecipeTree()
                && recipe.getInputs().size() <= RecipeFill.GRID_SIZE;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<StationMenu> context) {
        return recipe.getInputs().size() <= RecipeFill.GRID_SIZE && context.getInventory().canCraft(recipe);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<StationMenu> context) {
        StationMenu menu = context.getScreenHandler();
        List<EmiIngredient> inputs = recipe.getInputs();
        if (!menu.isClientSide() || inputs.size() > RecipeFill.GRID_SIZE) {
            return false;
        }
        List<RecipeFill.Requirement> requirements = new ArrayList<>(RecipeFill.GRID_SIZE);
        for (EmiIngredient input : inputs) {
            requirements.add(requirement(input));
        }
        RecipeFill.Plan plan = RecipeFill.plan(menu, requirements);
        if (!plan.complete() || plan.templates().stream().allMatch(ItemStack::isEmpty)) {
            return false;
        }
        int sets = RecipeFill.targetSets(menu, plan.templates(), context.getAmount());
        RecipeFill.send(menu, plan.templates(), sets);
        Minecraft.getInstance().setScreen(context.getScreen());
        switch (context.getDestination()) {
            case CURSOR -> PendingCraft.schedule(menu, plan.templates(), ClickType.PICKUP);
            case INVENTORY -> PendingCraft.schedule(menu, plan.templates(), ClickType.QUICK_MOVE);
            default -> {
            }
        }
        return true;
    }

    @Nullable
    private static RecipeFill.Requirement requirement(EmiIngredient ingredient) {
        if (ingredient.isEmpty()) {
            return null;
        }
        List<EmiStack> options = new ArrayList<>();
        Set<Item> items = new LinkedHashSet<>();
        for (EmiStack option : ingredient.getEmiStacks()) {
            ItemStack stack = option.getItemStack();
            if (!stack.isEmpty()) {
                options.add(option);
                items.add(stack.getItem());
            }
        }
        if (options.isEmpty()) {
            return null;
        }
        return new RecipeFill.Requirement(items, stack -> {
            EmiStack available = EmiStack.of(stack);
            for (EmiStack option : options) {
                if (available.isEqual(option)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<StationMenu> context, List<Widget> widgets, GuiGraphics draw) {
        List<EmiIngredient> inputs = recipe.getInputs();
        List<Boolean> states = context.getInventory().getCraftAvailability(recipe);
        if (states.size() != inputs.size()) {
            return;
        }
        Map<EmiIngredient, Boolean> available = new IdentityHashMap<>();
        for (int i = 0; i < inputs.size(); i++) {
            available.put(inputs.get(i), states.get(i));
        }
        RenderSystem.enableDepthTest();
        for (Widget widget : widgets) {
            if (widget instanceof SlotWidget slot && slot.getRecipe() == null) {
                EmiIngredient stack = slot.getStack();
                Boolean state = available.get(stack);
                if (state != null && !state && !stack.isEmpty()) {
                    Bounds bounds = slot.getBounds();
                    draw.fill(bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), MISSING_COLOR);
                }
            }
        }
    }
}
