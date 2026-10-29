package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.crafting.KeepColourShapelessRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipeSerializers {
    public static final Entry<RecipeSerializer<KeepColourShapelessRecipe>> KEEP_COLOUR_SHAPELESS = new Entry<>(JustEnoughCraftingTables.id("keep_colour_shapeless"),
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, JustEnoughCraftingTables.id("keep_colour_shapeless"), new KeepColourShapelessRecipe.Serializer()));
    public static void init() {}
}
