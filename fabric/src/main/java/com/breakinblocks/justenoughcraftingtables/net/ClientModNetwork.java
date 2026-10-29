package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.platform.ClientNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Player;

public final class ClientModNetwork {
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SidesPayload.TYPE, (payload, context) -> handleSides(payload, context.player()));
        ClientPlayNetworking.registerGlobalReceiver(ViewStatePayload.TYPE, (payload, context) -> handleViewState(payload, context.player()));
        ClientPlayNetworking.registerGlobalReceiver(SummaryPayload.TYPE, (payload, context) -> handleSummary(payload, context.player()));
    }

    private static void handleSides(SidesPayload payload, Player player) {
        StationMenu menu = ModNetwork.menu(player, payload.containerId());
        if (menu != null) {
            menu.applySides(payload, ClientNetwork::sendToServer);
        }
    }

    private static void handleViewState(ViewStatePayload payload, Player player) {
        StationMenu menu = ModNetwork.menu(player, payload.containerId());
        if (menu != null) {
            menu.applyViewState(payload);
        }
    }

    private static void handleSummary(SummaryPayload payload, Player player) {
        StationMenu menu = ModNetwork.menu(player, payload.containerId());
        if (menu != null) {
            menu.applySummary(payload);
        }
    }
}
