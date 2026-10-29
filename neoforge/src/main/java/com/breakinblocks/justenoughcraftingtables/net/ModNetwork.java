package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID)
public final class ModNetwork {
    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(JustEnoughCraftingTables.MOD_ID).versioned("2");
        registrar.playToServer(ViewPayload.TYPE, ViewPayload.STREAM_CODEC, ModNetwork::handleView);
        registrar.playToServer(FillRecipePayload.TYPE, FillRecipePayload.STREAM_CODEC, ModNetwork::handleFill);
        registrar.playToClient(SidesPayload.TYPE, SidesPayload.STREAM_CODEC, ModNetwork::handleSides);
        registrar.playToClient(ViewStatePayload.TYPE, ViewStatePayload.STREAM_CODEC, ModNetwork::handleViewState);
        registrar.playToClient(SummaryPayload.TYPE, SummaryPayload.STREAM_CODEC, ModNetwork::handleSummary);
    }

    @Nullable
    private static StationMenu menu(Player player, int containerId) {
        if (player != null && player.containerMenu instanceof StationMenu menu && menu.containerId == containerId) {
            return menu;
        }
        return null;
    }

    private static void handleView(ViewPayload payload, IPayloadContext context) {
        StationMenu menu = menu(context.player(), payload.containerId());
        if (menu != null && menu.stillValid(context.player())) {
            menu.handleViewRequest(payload.requestId(), payload.sidesVersion(), payload.request());
        }
    }

    private static void handleFill(FillRecipePayload payload, IPayloadContext context) {
        StationMenu menu = menu(context.player(), payload.containerId());
        if (menu != null && menu.stillValid(context.player()) && !context.player().isSpectator()) {
            menu.handleFill(payload);
        }
    }

    private static void handleSides(SidesPayload payload, IPayloadContext context) {
        StationMenu menu = menu(context.player(), payload.containerId());
        if (menu != null) {
            menu.applySides(payload, ClientPacketDistributor::sendToServer);
        }
    }

    private static void handleViewState(ViewStatePayload payload, IPayloadContext context) {
        StationMenu menu = menu(context.player(), payload.containerId());
        if (menu != null) {
            menu.applyViewState(payload);
        }
    }

    private static void handleSummary(SummaryPayload payload, IPayloadContext context) {
        StationMenu menu = menu(context.player(), payload.containerId());
        if (menu != null) {
            menu.applySummary(payload);
        }
    }
}
