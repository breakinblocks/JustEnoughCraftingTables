package com.breakinblocks.justenoughcraftingtables.platform;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;
public final class Platform {
public static boolean canReach(Player player, BlockPos pos) { return player.canReach(pos, 4.0); }
public static boolean isLoaded(String id) { return ModList.get().isLoaded(id); }
public static String modName(String id) { return ModList.get().getModContainerById(id).map(c->c.getModInfo().getDisplayName()).orElse(id); }
public static boolean allowAccess(Player player,BlockPos pos,BlockEntity inventory) { return !MinecraftForge.EVENT_BUS.post(new StationAccessEvent(player,pos,inventory)); }
public static void setCraftingPlayer(Player player) { ForgeHooks.setCraftingPlayer(player); }
public static void openMenu(ServerPlayer player,StationBlockEntity station) { NetworkHooks.openScreen(player,station,buffer->StationMenu.writeInitialData(player,station,buffer)); }
}
