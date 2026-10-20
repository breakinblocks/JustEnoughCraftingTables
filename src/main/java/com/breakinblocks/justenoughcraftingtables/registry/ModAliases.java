package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModAliases {
    public static final String LEGACY_NAMESPACE = "craftingstation";

    private ModAliases() {
    }

    public static ResourceLocation legacy(String path) {
        return ResourceLocation.fromNamespaceAndPath(LEGACY_NAMESPACE, path);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerAliases(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            ForgeRegistry<Block> registry = (ForgeRegistry<Block>) ForgeRegistries.BLOCKS;
            registry.addAlias(legacy("crafting_station"), ModBlocks.CRAFTING_STATION.getId());
            registry.addAlias(legacy("crafting_station_slab"), ModBlocks.CRAFTING_STATION_SLAB.getId());
        } else if (event.getRegistryKey().equals(Registries.ITEM)) {
            ForgeRegistry<Item> registry = (ForgeRegistry<Item>) ForgeRegistries.ITEMS;
            registry.addAlias(legacy("crafting_station"), ModBlocks.CRAFTING_STATION.getId());
            registry.addAlias(legacy("crafting_station_slab"), ModBlocks.CRAFTING_STATION_SLAB.getId());
        } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            ForgeRegistry<BlockEntityType<?>> registry = (ForgeRegistry<BlockEntityType<?>>) ForgeRegistries.BLOCK_ENTITY_TYPES;
            registry.addAlias(legacy("crafting_station"), ModBlockEntities.CRAFTING_STATION.getId());
        }
    }
}
