package com.breakinblocks.justenoughcraftingtables.registry;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JustEnoughCraftingTables.MOD_ID);

    static {
        ResourceLocation station = JustEnoughCraftingTables.id("crafting_station");
        ResourceLocation slab = JustEnoughCraftingTables.id("crafting_station_slab");
        ITEMS.addAlias(ModAliases.legacy("crafting_station"), station);
        ITEMS.addAlias(ModAliases.legacy("crafting_station_slab"), slab);
    }

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
