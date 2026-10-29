package com.breakinblocks.justenoughcraftingtables.integration;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.integration.craftingtweaks.CraftingTweaksIntegration;
import com.breakinblocks.justenoughcraftingtables.integration.polymorph.PolymorphIntegration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class Integrations {
    public static final String CRAFTING_TWEAKS = "craftingtweaks";
    public static final String POLYMORPH = "polymorph";

    private Integrations() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded(CRAFTING_TWEAKS)) {
                CraftingTweaksIntegration.register();
                JustEnoughCraftingTables.LOGGER.info("Registered the Crafting Tweaks grid provider for the crafting station");
            }
            if (ModList.get().isLoaded(POLYMORPH)) {
                PolymorphIntegration.register();
                JustEnoughCraftingTables.LOGGER.info("Crafting station recipes now go through Polymorph's per-player recipe selection");
            }
        });
    }
}
