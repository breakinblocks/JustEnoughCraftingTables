package com.breakinblocks.justenoughcraftingtables.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Called before reading neighboring inventories. Return false to deny access. */
@FunctionalInterface
public interface StationAccessEvent {
    Event<StationAccessEvent> EVENT = EventFactory.createArrayBacked(StationAccessEvent.class, callbacks -> (player, station, inventory) -> {
        for (StationAccessEvent callback : callbacks) {
            if (!callback.allow(player, station, inventory)) return false;
        }
        return true;
    });
    boolean allow(Player player, BlockPos station, BlockEntity inventory);
}
