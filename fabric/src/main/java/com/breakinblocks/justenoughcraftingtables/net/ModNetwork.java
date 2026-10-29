package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jspecify.annotations.Nullable;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ViewPayload.TYPE, ViewPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FillRecipePayload.TYPE, FillRecipePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SidesPayload.TYPE, SidesPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ViewStatePayload.TYPE, ViewStatePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SummaryPayload.TYPE, SummaryPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ViewPayload.TYPE, (payload, context) -> handleView(payload, context.player()));
        ServerPlayNetworking.registerGlobalReceiver(FillRecipePayload.TYPE, (payload, context) -> handleFill(payload, context.player()));
    }
    @Nullable
    static StationMenu menu(Player player, int containerId) {
        if (player != null && player.containerMenu instanceof StationMenu menu && menu.containerId == containerId) {
            return menu;
        }
        return null;
    }

    private static void handleView(ViewPayload payload, Player player) {
        StationMenu menu = menu(player, payload.containerId());
        if (menu != null && menu.stillValid(player)) {
            menu.handleViewRequest(payload.requestId(), payload.sidesVersion(), payload.request());
        }
    }

    private static void handleFill(FillRecipePayload payload, Player player) {
        StationMenu menu = menu(player, payload.containerId());
        if (menu != null && menu.stillValid(player) && !player.isSpectator()) {
            menu.handleFill(payload);
        }
    }

}
