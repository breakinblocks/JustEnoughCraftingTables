package com.breakinblocks.justenoughcraftingtables.integration.emi;

import com.breakinblocks.justenoughcraftingtables.integration.RecipeFill;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

final class PendingCraft {
    private static final int TIMEOUT_TICKS = 40;

    private static boolean listening;

    @Nullable
    private static WeakReference<StationMenu> menu;

    private static List<ItemStack> templates = List.of();
    private static ClickType clickType = ClickType.PICKUP;
    private static int ticks;

    private PendingCraft() {
    }

    static void schedule(StationMenu target, List<ItemStack> pattern, ClickType type) {
        if (!listening) {
            listening = true;
            MinecraftForge.EVENT_BUS.addListener(PendingCraft::onClientTick);
        }
        menu = new WeakReference<>(target);
        templates = List.copyOf(pattern);
        clickType = type;
        ticks = 0;
    }

    private static void clear() {
        menu = null;
        templates = List.of();
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (menu == null) {
            return;
        }
        StationMenu target = menu.get();
        Minecraft minecraft = Minecraft.getInstance();
        if (target == null || minecraft.player == null || minecraft.gameMode == null
                || minecraft.player.containerMenu != target || ++ticks > TIMEOUT_TICKS) {
            clear();
            return;
        }
        if (!RecipeFill.gridMatches(target, templates) || !target.getSlot(StationMenu.RESULT_SLOT).hasItem()) {
            return;
        }
        minecraft.gameMode.handleInventoryMouseClick(target.containerId, StationMenu.RESULT_SLOT, 0, clickType, minecraft.player);
        clear();
    }
}
