package com.breakinblocks.justenoughcraftingtables.platform;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.menu.StationOpeningData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class Platform {
    public static boolean isLoaded(String id) { return FabricLoader.getInstance().isModLoaded(id); }
    public static String modName(String id) { return FabricLoader.getInstance().getModContainer(id).map(c -> c.getMetadata().getName()).orElse(id); }
    public static void setCraftingPlayer(Player player) { }
    public static boolean allowAccess(Player player, BlockPos station, BlockEntity inventory) { return StationAccessEvent.EVENT.invoker().allow(player, station, inventory); }
    public static boolean canSend(ServerPlayer player, CustomPacketPayload payload) { return ServerPlayNetworking.canSend(player, payload.type()); }
    public static void send(ServerPlayer player, CustomPacketPayload payload) { ServerPlayNetworking.send(player, payload); }
    public static void openMenu(ServerPlayer player, StationBlockEntity station) {
        player.openMenu(new ExtendedScreenHandlerFactory<StationOpeningData>() {
            public Component getDisplayName() { return station.getDisplayName(); }
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player owner) { return station.createMenu(id, inventory, owner); }
            public StationOpeningData getScreenOpeningData(ServerPlayer owner) { return ((StationMenu) owner.containerMenu).openingData(); }
        });
        if (isLoaded("polymorph") && player.containerMenu instanceof StationMenu menu) {
            menu.updateResult(true);
        }
    }
}
