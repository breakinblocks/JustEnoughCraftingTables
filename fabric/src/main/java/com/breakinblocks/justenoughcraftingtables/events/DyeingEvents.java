package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.common.block.StationBlocks;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public final class DyeingEvents {
    private DyeingEvents() {
    }

    public static InteractionResult use(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!StationBlocks.isStation(state) || !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        DyeColor dye = stack.getItem() instanceof DyeItem item ? item.getDyeColor() : null;
        if (dye == null) {
            return InteractionResult.PASS;
        }
        StationColor colour = StationColor.of(dye);
        if (state.getValue(StationColor.PROPERTY) == colour || !player.mayBuild() || !player.mayUseItemAt(pos, hit.getDirection(), stack)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(StationColor.PROPERTY, colour), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
