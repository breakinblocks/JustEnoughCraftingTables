package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.WindowSlot;
import net.minecraft.world.inventory.ClickAction;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

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
