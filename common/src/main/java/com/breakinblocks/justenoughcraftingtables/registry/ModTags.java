package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModTags {
    public static final TagKey<BlockEntityType<?>> EXCLUDED =
            TagKey.create(Registries.BLOCK_ENTITY_TYPE, JustEnoughCraftingTables.id("excluded"));
    public static final TagKey<BlockEntityType<?>> LEGACY_BLACKLISTED =
            TagKey.create(Registries.BLOCK_ENTITY_TYPE, ModAliases.legacy("blacklisted"));
    public static final TagKey<BlockEntityType<?>> TOUCHING_FACE_ACCESS =
            TagKey.create(Registries.BLOCK_ENTITY_TYPE, JustEnoughCraftingTables.id("touching_face_access"));

    private ModTags() {
    }

    public static boolean is(BlockEntityType<?> type, TagKey<BlockEntityType<?>> tag) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.wrapAsHolder(type).is(tag);
    }

    public static boolean isExcluded(BlockEntityType<?> type) {
        return is(type, EXCLUDED) || is(type, LEGACY_BLACKLISTED);
    }
}
