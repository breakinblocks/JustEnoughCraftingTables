package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID)
public final class CommonEvents {
    private static final int LOG_INTERVAL = 1200;
    private static int ticks;
    private static long totalLegacy;

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ticks = 0;
        totalLegacy = 0;
        logLegacy();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
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
