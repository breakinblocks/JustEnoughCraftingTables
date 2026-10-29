package com.breakinblocks.justenoughcraftingtables.client.mixin;

import com.breakinblocks.justenoughcraftingtables.client.gui.CompactCount;
import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.menu.WindowSlot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractContainerScreen.class)
public abstract class WindowCountMixin {
    @ModifyArg(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"), index = 4)
    private String compactCount(Font font, ItemStack stack, int x, int y, String text) {
        if (!((Object) this instanceof StationScreen screen) || text != null
                || !JustEnoughCraftingTablesConfig.CLIENT.compactCounts.get() || stack.getCount() <= 999) return text;
        for (int i = StationMenu.WINDOW_START; i < StationMenu.WINDOW_END; i++) {
            WindowSlot slot = (WindowSlot) screen.getMenu().getSlot(i);
            if (slot.x == x && slot.y == y) {
                return CompactCount.format(stack.getCount());
            }
        }
        return text;
    }
}
