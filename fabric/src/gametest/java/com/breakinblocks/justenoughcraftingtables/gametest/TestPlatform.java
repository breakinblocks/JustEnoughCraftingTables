package com.breakinblocks.justenoughcraftingtables.gametest;
import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ClickType;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.registry.FuelRegistry;
public final class TestPlatform {
    public static ServerPlayer player(GameTestHelper helper) { return helper.makeMockServerPlayerInLevel(); }
    public static Runnable deny(BlockEntity inventory) {
        AtomicBoolean active = new AtomicBoolean(true);
        StationAccessEvent.EVENT.register((player, station, target) -> !active.get() || target != inventory);
        return () -> active.set(false);
    }
    public static void overrideClick(StationMenu menu, ServerPlayer player) {
        menu.clicked(StationMenu.WINDOW_START + 1, 1, ClickType.PICKUP, player);
    }
    public static int burnTime(ItemStack stack) { return FuelRegistry.INSTANCE.get(stack.getItem()); }
}
