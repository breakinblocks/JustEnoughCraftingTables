package com.breakinblocks.justenoughcraftingtables.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ClientNetwork {
    public static void send(CustomPacketPayload payload) { ClientPlayNetworking.send(payload); }
    public static void onTick(Runnable callback) { ClientTickEvents.END_CLIENT_TICK.register(client -> callback.run()); }
}
