package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class CommonEvents {
    private static final int LOG_INTERVAL = 1200;
    private static int ticks;
    private static long totalLegacy;

    private CommonEvents() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> onServerStarted());
        ServerTickEvents.END_SERVER_TICK.register(server -> onServerTick());
    }

    private static void onServerStarted() {
        ticks = 0;
        totalLegacy = 0;
        logLegacy();
    }

    private static void onServerTick() {
        if (++ticks >= LOG_INTERVAL) {
            ticks = 0;
            logLegacy();
        }
    }

    private static void logLegacy() {
        int count = StationBlockEntity.takeLegacyLoadedCount();
        if (count <= 0) {
            return;
        }
        totalLegacy += count;
        JustEnoughCraftingTables.LOGGER.info(
                "Loaded {} crafting station block entities saved by Crafting Station; they are converted when their chunks save ({} since the server started)",
                count, totalLegacy);
    }
}
