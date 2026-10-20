package com.breakinblocks.justenoughcraftingtables.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/** Fired on the server before a station accesses a neighboring inventory. Protection integrations may cancel it. */
@Cancelable
public final class StationAccessEvent extends PlayerEvent {
    private final BlockPos stationPos;
    private final BlockEntity inventory;

    public StationAccessEvent(Player player, BlockPos stationPos, BlockEntity inventory) {
        super(player);
        this.stationPos = stationPos.immutable();
        this.inventory = inventory;
    }

    public BlockPos stationPos() {
        return stationPos;
    }

    public BlockEntity inventory() {
        return inventory;
    }
}
