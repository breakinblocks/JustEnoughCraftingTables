package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.WindowSlot;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID)
public final class WindowSlotEvents {
    private WindowSlotEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemStackedOnOther(ItemStackedOnOtherEvent event) {
        if (event.getSlot() instanceof WindowSlot slot) {
            slot.pickup(event.getPlayer(), event.getClickAction() == ClickAction.PRIMARY ? 0 : 1);
            event.setCanceled(true);
        }
    }
}
