package com.breakinblocks.justenoughcraftingtables.integration.craftingtweaks;

import net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI;

public final class CraftingTweaksIntegration {
    private CraftingTweaksIntegration() {
    }

    public static void register() {
        CraftingTweaksAPI.registerCraftingGridProvider(new StationGridProvider());
    }
}
