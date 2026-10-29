package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.inventory.ContainerInput;

final class TestPlatform {
    static Runnable deny(BlockEntity inventory) {
        AtomicBoolean active = new AtomicBoolean(true);
        StationAccessEvent.EVENT.register((player, station, target) -> !active.get() || target != inventory);
        return () -> active.set(false);
    }
    static void rightClickWindow(GameTestHelper helper, StationMenu menu, ServerPlayer player) {
        menu.clicked(StationMenu.WINDOW_START + 1, 1, ContainerInput.PICKUP, player);
    }
}
