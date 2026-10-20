package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, JustEnoughCraftingTables.MOD_ID);

    private ModItems() {
    }

    @SubscribeEvent
    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            return;
        }
        event.accept(ModBlocks.CRAFTING_STATION.get());
        event.accept(ModBlocks.CRAFTING_STATION_SLAB.get());
        for (StationColor color : StationColor.values()) {
            if (color.isDyed()) {
                event.accept(StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION.get()), color));
            }
        }
        for (StationColor color : StationColor.values()) {
            if (color.isDyed()) {
                event.accept(StationColor.apply(new ItemStack(ModBlocks.CRAFTING_STATION_SLAB.get()), color));
            }
        }
    }
}
