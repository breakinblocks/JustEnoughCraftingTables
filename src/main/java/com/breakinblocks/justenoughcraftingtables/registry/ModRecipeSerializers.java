package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.crafting.KeepColourShapelessRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, JustEnoughCraftingTables.MOD_ID);

    public static final RegistryObject<KeepColourShapelessRecipe.Serializer> KEEP_COLOUR_SHAPELESS =
            SERIALIZERS.register("keep_colour_shapeless", KeepColourShapelessRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }
}
