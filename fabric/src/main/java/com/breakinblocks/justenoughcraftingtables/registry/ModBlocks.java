package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationBlock;
import com.breakinblocks.justenoughcraftingtables.common.block.StationBlocks;
import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.common.item.StationBlockItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {

    public static final Entry<StationBlock> CRAFTING_STATION = registerBlock("crafting_station",
            id -> new StationBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)
                    .mapColor(StationBlocks::mapColor)
                    .isSuffocating((state, level, pos) -> false)));

    public static final Entry<StationSlabBlock> CRAFTING_STATION_SLAB = registerBlock("crafting_station_slab",
            id -> new StationSlabBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE).mapColor(StationBlocks::mapColor)));

    public static void init() {}

    private ModBlocks() {
    }

    public static <T extends Block> Entry<T> registerBlock(String name, Function<ResourceLocation, T> factory) {
        ResourceLocation id = JustEnoughCraftingTables.id(name);
        Entry<T> block = new Entry<>(id, Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(id)));
        Registry.register(BuiltInRegistries.ITEM, id, new StationBlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
