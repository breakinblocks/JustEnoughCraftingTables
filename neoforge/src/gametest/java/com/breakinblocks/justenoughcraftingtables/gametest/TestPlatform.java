package com.breakinblocks.justenoughcraftingtables.gametest;
import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ClickType;
import java.util.function.Consumer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
public final class TestPlatform {
    public static ServerPlayer player(GameTestHelper helper) { return helper.makeMockServerPlayerInLevel(); }
    public static Runnable deny(BlockEntity inventory) {
        Consumer<StationAccessEvent> deny = event -> { if (event.inventory() == inventory) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(deny);
        return () -> NeoForge.EVENT_BUS.unregister(deny);
    }
    public static void overrideClick(StationMenu menu, ServerPlayer player) {
        var event = NeoForge.EVENT_BUS.post(new ItemStackedOnOtherEvent(menu.getCarried(), ItemStack.EMPTY,
                menu.windowSlot(1), ClickAction.SECONDARY, player, SlotAccess.NULL));
        if (!event.isCanceled()) throw new AssertionError("Item override reached window slot");
    }
    public static int burnTime(ItemStack stack) { return stack.getBurnTime(RecipeType.SMELTING); }
}
