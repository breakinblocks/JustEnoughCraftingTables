package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.LayeredCauldronBlock;

public final class ModBusEvents {
    public static final CauldronInteraction WASH_STATION = (state, level, pos, player, hand, stack) -> {
        if (!StationColor.of(stack).isDyed()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            ItemStack washed = StationColor.apply(stack.copyWithCount(1), StationColor.NONE);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, washed, false));
            player.awardStat(Stats.USE_CAULDRON);
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    };

    private ModBusEvents() {
    }

}
