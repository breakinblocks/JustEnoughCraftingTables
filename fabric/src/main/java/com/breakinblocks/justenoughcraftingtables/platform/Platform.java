package com.breakinblocks.justenoughcraftingtables.platform;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
public final class Platform {
public static boolean canReach(Player player, BlockPos pos) { return player.distanceToSqr(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5) <= 64.0; }
public static boolean isLoaded(String id) { return FabricLoader.getInstance().isModLoaded(id); }
public static String modName(String id) { return FabricLoader.getInstance().getModContainer(id).map(c->c.getMetadata().getName()).orElse(id); }
public static boolean allowAccess(Player player,BlockPos pos,BlockEntity inventory) { return StationAccessEvent.EVENT.invoker().allow(player,pos,inventory); }
public static void setCraftingPlayer(Player player) {}
public static void openMenu(ServerPlayer player,StationBlockEntity station) {
    player.openMenu(new ExtendedScreenHandlerFactory() {
        public Component getDisplayName() { return station.getDisplayName(); }
        public AbstractContainerMenu createMenu(int id,Inventory inventory,Player owner) { return station.createMenu(id,inventory,owner); }
        public void writeScreenOpeningData(ServerPlayer owner,FriendlyByteBuf buffer) { StationMenu.writeInitialData(owner,station,buffer); }
    });
    if(isLoaded("polymorph") && player.containerMenu instanceof StationMenu menu) menu.updateResult(true);
}
}
