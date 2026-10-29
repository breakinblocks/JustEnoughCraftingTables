package com.breakinblocks.justenoughcraftingtables.common.crafting;

import com.breakinblocks.justenoughcraftingtables.common.block.StationBlock;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.registry.ModRecipeSerializers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public class KeepColourShapelessRecipe extends NormalCraftingRecipe {
    public static final MapCodec<KeepColourShapelessRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients))
            .apply(instance, KeepColourShapelessRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KeepColourShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(9)), recipe -> recipe.ingredients,
            KeepColourShapelessRecipe::new);

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;
    private final ShapelessRecipe delegate;

    public KeepColourShapelessRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
            ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = List.copyOf(ingredients);
        this.delegate = new ShapelessRecipe(commonInfo, bookInfo, result, this.ingredients);
    }

    @Override
    public RecipeSerializer<KeepColourShapelessRecipe> getSerializer() {
        return ModRecipeSerializers.KEEP_COLOUR_SHAPELESS.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return delegate.placementInfo();
    }

    @Override
    public List<RecipeDisplay> display() {
        return delegate.display();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return delegate.matches(input, level) && commonColour(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        StationColor colour = commonColour(input);
        ItemStack out = result.create();
        return colour == null ? out : StationColor.apply(out, colour);
    }

    @Nullable
    public static StationColor commonColour(CraftingInput input) {
        StationColor found = null;
        for (ItemStack stack : input.items()) {
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

}
