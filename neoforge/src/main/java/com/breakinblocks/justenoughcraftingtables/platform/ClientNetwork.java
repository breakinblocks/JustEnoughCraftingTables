package com.breakinblocks.justenoughcraftingtables.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class ClientNetwork {
    public static void send(CustomPacketPayload payload) { PacketDistributor.sendToServer(payload); }
    public static void onTick(Runnable callback) { NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> callback.run()); }
}
