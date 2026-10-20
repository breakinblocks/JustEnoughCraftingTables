package com.breakinblocks.justenoughcraftingtables.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Fired on the server before a station accesses a neighboring inventory. Protection integrations may cancel it. */
public final class StationAccessEvent extends PlayerEvent implements ICancellableEvent {
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
