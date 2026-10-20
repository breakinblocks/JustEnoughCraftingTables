package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

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
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
