package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationBlock;
import com.breakinblocks.justenoughcraftingtables.common.block.StationBlocks;
import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.common.item.StationBlockItem;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JustEnoughCraftingTables.MOD_ID);

    public static final DeferredBlock<StationBlock> CRAFTING_STATION = registerBlock("crafting_station",
            id -> new StationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(ResourceKey.create(Registries.BLOCK, id))
                    .mapColor(StationBlocks::mapColor)
                    .isSuffocating((state, level, pos) -> false)));

    public static final DeferredBlock<StationSlabBlock> CRAFTING_STATION_SLAB = registerBlock("crafting_station_slab",
            id -> new StationSlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(ResourceKey.create(Registries.BLOCK, id)).mapColor(StationBlocks::mapColor)));

    static {
        BLOCKS.addAlias(ModAliases.legacy("crafting_station"), CRAFTING_STATION.getId());
        BLOCKS.addAlias(ModAliases.legacy("crafting_station_slab"), CRAFTING_STATION_SLAB.getId());
    }

    private ModBlocks() {
    }

    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<Identifier, T> factory) {
        DeferredBlock<T> block = BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, id -> new StationBlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
        return block;
    }
}
