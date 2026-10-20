package com.breakinblocks.justenoughcraftingtables.integration;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.integration.craftingtweaks.CraftingTweaksIntegration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID)
public final class Integrations {
    public static final String CRAFTING_TWEAKS = "craftingtweaks";

    private Integrations() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded(CRAFTING_TWEAKS)) {
                CraftingTweaksIntegration.register();
                JustEnoughCraftingTables.LOGGER.info("Registered the Crafting Tweaks grid provider for the crafting station");
            }
        });
    }
}
