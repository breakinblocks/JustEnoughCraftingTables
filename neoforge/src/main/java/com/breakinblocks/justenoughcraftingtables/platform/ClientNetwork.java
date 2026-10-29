package com.breakinblocks.justenoughcraftingtables.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class ClientNetwork {
    public static void sendToServer(CustomPacketPayload payload) { ClientPacketDistributor.sendToServer(payload); }
}
