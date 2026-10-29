package com.breakinblocks.justenoughcraftingtables;

import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.events.CommonEvents;
import com.breakinblocks.justenoughcraftingtables.events.DyeingEvents;
import com.breakinblocks.justenoughcraftingtables.events.ModBusEvents;
import com.breakinblocks.justenoughcraftingtables.integration.craftingtweaks.CraftingTweaksIntegration;
import com.breakinblocks.justenoughcraftingtables.integration.polymorph.PolymorphIntegration;
import com.breakinblocks.justenoughcraftingtables.net.ModNetwork;
import com.breakinblocks.justenoughcraftingtables.platform.Platform;
import com.breakinblocks.justenoughcraftingtables.registry.ModAliases;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import com.breakinblocks.justenoughcraftingtables.registry.ModRecipeSerializers;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

public final class JustEnoughCraftingTables implements ModInitializer {
    public static final String MOD_ID = "justenoughcraftingtables";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ResourceLocation id(String path) { return new ResourceLocation(MOD_ID, path); }
    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModRecipeSerializers.init();
        JustEnoughCraftingTablesConfig.pageSize();
        for (String name : new String[] {"crafting_station", "crafting_station_slab"}) {
            BuiltInRegistries.BLOCK.addAlias(ModAliases.legacy(name), id(name));
            BuiltInRegistries.ITEM.addAlias(ModAliases.legacy(name), id(name));
        }
        BuiltInRegistries.BLOCK_ENTITY_TYPE.addAlias(ModAliases.legacy("crafting_station"), id("crafting_station"));
        ModNetwork.register();
        CommonEvents.register();
        UseBlockCallback.EVENT.register(DyeingEvents::use);
        CauldronInteraction.WATER.put(ModBlocks.CRAFTING_STATION.get().asItem(), ModBusEvents.WASH_STATION);
        CauldronInteraction.WATER.put(ModBlocks.CRAFTING_STATION_SLAB.get().asItem(), ModBusEvents.WASH_STATION);
        FuelRegistry.INSTANCE.add(ModBlocks.CRAFTING_STATION.get(), 300);
        FuelRegistry.INSTANCE.add(ModBlocks.CRAFTING_STATION_SLAB.get(), 150);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(ModBlocks.CRAFTING_STATION.get());
            entries.accept(ModBlocks.CRAFTING_STATION_SLAB.get());
            for (var block : new Block[] {ModBlocks.CRAFTING_STATION.get(), ModBlocks.CRAFTING_STATION_SLAB.get()}) {
                for (StationColor color : StationColor.values()) {
                    if (color.isDyed()) entries.accept(StationColor.apply(new ItemStack(block), color));
                }
            }
        });
        if (Platform.isLoaded("craftingtweaks")) CraftingTweaksIntegration.register();
        if (Platform.isLoaded("polymorph")) PolymorphIntegration.register();
    }
}
