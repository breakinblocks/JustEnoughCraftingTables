package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final Entry<BlockEntityType<StationBlockEntity>> CRAFTING_STATION = new Entry<>(JustEnoughCraftingTables.id("crafting_station"),
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, JustEnoughCraftingTables.id("crafting_station"),
            FabricBlockEntityTypeBuilder.create(StationBlockEntity::new, ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get()).build()));
    public static void init() {}
}
