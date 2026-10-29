package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationBlock;
import com.breakinblocks.justenoughcraftingtables.common.block.StationBlocks;
import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.common.item.StationBlockItem;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, JustEnoughCraftingTables.MOD_ID);

    public static final RegistryObject<StationBlock> CRAFTING_STATION = registerBlock("crafting_station",
            () -> new StationBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)
                    .mapColor(StationBlocks::mapColor)
                    .isSuffocating((state, level, pos) -> false)));

    public static final RegistryObject<StationSlabBlock> CRAFTING_STATION_SLAB = registerBlock("crafting_station_slab",
            () -> new StationSlabBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE).mapColor(StationBlocks::mapColor)));

    private ModBlocks() {
    }

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> supplier) {
        RegistryObject<T> block = BLOCKS.register(name, supplier);
        ModItems.ITEMS.register(name, () -> new StationBlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
