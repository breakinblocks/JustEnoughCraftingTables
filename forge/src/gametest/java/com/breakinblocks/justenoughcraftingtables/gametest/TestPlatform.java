package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
public final class TestPlatform {
    public static ServerPlayer player(GameTestHelper helper) { return TestSupport.serverPlayer(helper); }
    public static Runnable deny(BlockEntity inventory) {
        Consumer<StationAccessEvent> deny = event -> { if (event.inventory() == inventory) event.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(deny);
        return () -> MinecraftForge.EVENT_BUS.unregister(deny);
    }
    public static void overrideClick(GameTestHelper helper, StationMenu menu, ServerPlayer player) {
        var event = new ItemStackedOnOtherEvent(menu.getCarried(), ItemStack.EMPTY,
                menu.windowSlot(1), ClickAction.SECONDARY, player, SlotAccess.NULL);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) throw new AssertionError("Item override reached window slot");
    }
    public static int burnTime(ItemStack stack) { return stack.getBurnTime(RecipeType.SMELTING); }
}
