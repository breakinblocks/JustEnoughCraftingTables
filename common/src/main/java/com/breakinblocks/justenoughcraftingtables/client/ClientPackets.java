package com.breakinblocks.justenoughcraftingtables.client;

import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.net.ModNetwork;
import com.breakinblocks.justenoughcraftingtables.net.SidesPayload;
import com.breakinblocks.justenoughcraftingtables.net.SummaryPayload;
import com.breakinblocks.justenoughcraftingtables.net.ViewStatePayload;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientPackets {
    private ClientPackets() {
    }

    @Nullable
    private static StationMenu menu(int id) {
        Player player = Minecraft.getInstance().player;
        return player != null && player.containerMenu instanceof StationMenu menu && menu.containerId == id ? menu : null;
    }

    public static void sides(SidesPayload payload) {
        StationMenu menu = menu(payload.containerId());
        if (menu != null) menu.applySides(payload, ModNetwork::sendToServer);
    }

    public static void view(ViewStatePayload payload) {
        StationMenu menu = menu(payload.containerId());
        if (menu != null) menu.applyViewState(payload);
    }

    public static void summary(SummaryPayload payload) {
        StationMenu menu = menu(payload.containerId());
        if (menu != null) menu.applySummary(payload);
    }
}
