package com.breakinblocks.justenoughcraftingtables.common.block;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;

public final class StationBlocks {
    private StationBlocks() {
    }

    public static boolean isStation(BlockState state) {
        return state.is(ModBlocks.CRAFTING_STATION.get()) || state.is(ModBlocks.CRAFTING_STATION_SLAB.get());
    }

    public static InteractionResult open(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof StationBlockEntity station && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(station);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    public static MenuProvider menuProvider(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StationBlockEntity station ? station : null;
    }

    public static void neighborChanged(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof StationBlockEntity station) {
            station.onNeighbourChanged();
        }
    }

    public static MapColor mapColor(BlockState state) {
        DyeColor dye = state.getValue(StationColor.PROPERTY).dye();
        return dye == null ? MapColor.WOOD : dye.getMapColor();
    }

    public static ItemStack cloneStack(Block block, BlockState state) {
        ItemStack stack = new ItemStack(block);
        StationColor.apply(stack, state.getValue(StationColor.PROPERTY));
        return stack;
    }
}
