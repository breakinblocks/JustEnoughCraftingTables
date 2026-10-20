package com.breakinblocks.justenoughcraftingtables.client;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.client.gui.ViewMemory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ViewMemory.clear();
    }
}
