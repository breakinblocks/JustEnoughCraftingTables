package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.function.Consumer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;

final class TestPlatform {
    static Runnable deny(BlockEntity inventory) {
        Consumer<StationAccessEvent> deny = event -> { if (event.inventory() == inventory) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(deny);
        return () -> NeoForge.EVENT_BUS.unregister(deny);
    }
    static void rightClickWindow(GameTestHelper helper, StationMenu menu, ServerPlayer player) {
        var event = NeoForge.EVENT_BUS.post(new ItemStackedOnOtherEvent(menu.getCarried(), ItemStack.EMPTY,
            menu.windowSlot(1), ClickAction.SECONDARY, player, SlotAccess.of(() -> ItemStack.EMPTY, stack -> {})));
        helper.assertTrue(event.isCanceled(), "An item click override reached a window slot");
    }
}
