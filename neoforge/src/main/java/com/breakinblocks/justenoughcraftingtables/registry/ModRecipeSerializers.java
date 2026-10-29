package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.crafting.KeepColourShapelessRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, JustEnoughCraftingTables.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KeepColourShapelessRecipe>> KEEP_COLOUR_SHAPELESS =
            SERIALIZERS.register("keep_colour_shapeless", () -> new RecipeSerializer<>(KeepColourShapelessRecipe.MAP_CODEC, KeepColourShapelessRecipe.STREAM_CODEC));

    private ModRecipeSerializers() {
    }
}
