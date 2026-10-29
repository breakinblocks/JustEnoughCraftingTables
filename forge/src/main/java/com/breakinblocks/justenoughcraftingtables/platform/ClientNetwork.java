package com.breakinblocks.justenoughcraftingtables.platform;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
public final class ClientNetwork { public static void onTick(Runnable action) { MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> { if(event.phase==TickEvent.Phase.END) action.run(); }); } }
