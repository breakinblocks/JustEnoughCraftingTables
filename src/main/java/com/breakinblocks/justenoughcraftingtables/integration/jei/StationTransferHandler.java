package com.breakinblocks.justenoughcraftingtables.integration.jei;

import com.breakinblocks.justenoughcraftingtables.integration.RecipeFill;
import com.breakinblocks.justenoughcraftingtables.menu.ItemKey;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

public class StationTransferHandler implements IRecipeTransferHandler<StationMenu, CraftingRecipe> {
    private static final Component MISSING = Component.translatable("jei.tooltip.error.recipe.transfer.missing");

    private final IRecipeTransferHandlerHelper helper;

    public StationTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<? extends StationMenu> getContainerClass() {
        return StationMenu.class;
    }

    @Override
    public Optional<MenuType<StationMenu>> getMenuType() {
        return Optional.of(ModMenus.CRAFTING_STATION.get());
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(IRecipeTransferContext<CraftingRecipe, StationMenu> context, boolean doTransfer) {
        IRecipeTransferError error = transfer(context.getContainer(), context.getRecipe(), context.getRecipeSlots(), context.isMaxTransfer(), doTransfer);
        if (doTransfer) {
            context.completeRecipeTransfer(error == null ? RecipeTransferResult.SUCCESS : RecipeTransferResult.REJECTED);
        }
        return error;
    }

    @Nullable
    @Override
    @SuppressWarnings("removal")
    public IRecipeTransferError transferRecipe(
            StationMenu container, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        return transfer(container, recipe, recipeSlots, maxTransfer, doTransfer);
    }

    @Nullable
    private IRecipeTransferError transfer(
            StationMenu menu, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, boolean maxTransfer, boolean doTransfer) {
        if (!menu.isClientSide()) {
            return helper.createInternalError();
        }
        List<IRecipeSlotView> inputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
        if (inputs.size() > RecipeFill.GRID_SIZE) {
            return helper.createInternalError();
        }
        Map<Integer, Ingredient> ingredients = ingredientMap(recipe, inputs);
        List<RecipeFill.Requirement> requirements = new ArrayList<>(RecipeFill.GRID_SIZE);
        for (int i = 0; i < inputs.size(); i++) {
            requirements.add(requirement(inputs.get(i), ingredients == null ? null : ingredients.get(i)));
        }
        RecipeFill.Plan plan = RecipeFill.plan(menu, requirements);
        if (!plan.complete()) {
            List<IRecipeSlotView> missing = new ArrayList<>();
            for (int index : plan.missing()) {
                missing.add(inputs.get(index));
            }
            return helper.createUserErrorForMissingSlots(MISSING, missing);
        }
        if (plan.templates().stream().allMatch(ItemStack::isEmpty)) {
            return helper.createInternalError();
        }
        if (doTransfer) {
            int sets = maxTransfer ? 0 : RecipeFill.targetSets(menu, plan.templates(), 1);
            RecipeFill.send(menu, plan.templates(), sets);
        }
        return null;
    }

    @Nullable
    private Map<Integer, Ingredient> ingredientMap(CraftingRecipe recipe, List<IRecipeSlotView> inputs) {
        Map<Integer, Ingredient> map;
        try {
            map = helper.getGuiSlotIndexToIngredientMap(recipe);
        } catch (RuntimeException e) {
            return null;
        }
        Set<Integer> shown = new HashSet<>();
        for (int i = 0; i < inputs.size(); i++) {
            if (!inputs.get(i).isEmpty()) {
                shown.add(i);
            }
        }
        return map.keySet().equals(shown) ? map : null;
    }

    @Nullable
    private static RecipeFill.Requirement requirement(IRecipeSlotView view, @Nullable Ingredient ingredient) {
        List<ItemStack> stacks = view.getItemStacks().filter(stack -> !stack.isEmpty()).toList();
        if (stacks.isEmpty()) {
            return null;
        }
        Set<Item> items = new LinkedHashSet<>();
        Set<ItemKey> exact = new HashSet<>();
        for (ItemStack stack : stacks) {
            items.add(stack.getItem());
            exact.add(ItemKey.of(stack));
        }
        Predicate<ItemStack> test = ingredient != null ? ingredient : stack -> exact.contains(ItemKey.view(stack));
        return new RecipeFill.Requirement(items, test);
    }
}
