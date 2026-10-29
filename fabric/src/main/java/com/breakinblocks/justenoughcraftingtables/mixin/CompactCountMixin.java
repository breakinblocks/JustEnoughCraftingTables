package com.breakinblocks.justenoughcraftingtables.mixin;

import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import com.breakinblocks.justenoughcraftingtables.client.gui.CompactCount;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.menu.WindowSlot;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractContainerScreen.class)
public abstract class CompactCountMixin {
    @ModifyArg(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"), index = 4)
    private String stationCount(String original, @Local(argsOnly = true) Slot slot) {
        if ((Object) this instanceof StationScreen && slot instanceof WindowSlot && original == null
                && slot.getItem().getCount() > 999 && JustEnoughCraftingTablesConfig.CLIENT.compactCounts.get()) {
            return CompactCount.format(slot.getItem().getCount());
        }
        return original;
    }
}
