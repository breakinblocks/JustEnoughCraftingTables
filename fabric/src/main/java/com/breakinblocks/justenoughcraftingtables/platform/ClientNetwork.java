package com.breakinblocks.justenoughcraftingtables.platform;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
public final class ClientNetwork { public static void onTick(Runnable action) { ClientTickEvents.END_CLIENT_TICK.register(client -> action.run()); } }
