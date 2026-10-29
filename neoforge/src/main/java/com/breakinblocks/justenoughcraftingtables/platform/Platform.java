package com.breakinblocks.justenoughcraftingtables.platform;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;

public final class Platform {
    public static boolean isLoaded(String id) { return ModList.get().isLoaded(id); }
    public static String modName(String id) { return ModList.get().getModContainerById(id).map(c -> c.getModInfo().getDisplayName()).orElse(id); }
    public static void setCraftingPlayer(Player player) { CommonHooks.setCraftingPlayer(player); }
    public static boolean allowAccess(Player player, BlockPos station, BlockEntity inventory) { return !NeoForge.EVENT_BUS.post(new StationAccessEvent(player, station, inventory)).isCanceled(); }
    public static boolean canSend(ServerPlayer player, CustomPacketPayload payload) { return player.connection.hasChannel(payload); }
    public static void send(ServerPlayer player, CustomPacketPayload payload) { player.connection.send(payload); }
    public static void openMenu(ServerPlayer player, StationBlockEntity station) { player.openMenu(station); }
}
