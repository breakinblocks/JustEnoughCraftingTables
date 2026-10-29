package com.breakinblocks.justenoughcraftingtables.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ClientNetwork {
    public static void sendToServer(CustomPacketPayload payload) { ClientPlayNetworking.send(payload); }
}
