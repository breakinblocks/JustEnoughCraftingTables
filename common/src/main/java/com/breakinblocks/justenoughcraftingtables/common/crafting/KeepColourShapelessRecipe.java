package com.breakinblocks.justenoughcraftingtables.common.crafting;

import com.breakinblocks.justenoughcraftingtables.common.block.StationBlock;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.registry.ModRecipeSerializers;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class KeepColourShapelessRecipe extends ShapelessRecipe {
    private final ItemStack result;

    public KeepColourShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
        this.result = result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.KEEP_COLOUR_SHAPELESS.get(); }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        return super.matches(input, level) && commonColour(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        StationColor colour = commonColour(input);
        ItemStack out = result.copy();
        return colour == null ? out : StationColor.apply(out, colour);
    }

    @Nullable
    public static StationColor commonColour(CraftingContainer input) {
        StationColor found = null;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            Block block = Block.byItem(stack.getItem());
            if (!(block instanceof StationBlock) && !(block instanceof StationSlabBlock)) {
                continue;
            }
            StationColor colour = StationColor.of(stack);
            if (found == null) {
                found = colour;
            } else if (found != colour) {
                return null;
            }
        }
        return found == null ? StationColor.NONE : found;
    }

    public static final class Serializer implements RecipeSerializer<KeepColourShapelessRecipe> {
        private final ShapelessRecipe.Serializer vanilla = new ShapelessRecipe.Serializer();

        private static KeepColourShapelessRecipe convert(ShapelessRecipe recipe) {
            return new KeepColourShapelessRecipe(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
        }

        @Override
        public KeepColourShapelessRecipe fromJson(ResourceLocation id, JsonObject json) { return convert(vanilla.fromJson(id, json)); }

        @Override
        public KeepColourShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) { return convert(vanilla.fromNetwork(id, buffer)); }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, KeepColourShapelessRecipe recipe) { vanilla.toNetwork(buffer, recipe); }
    }
}
