package com.breakinblocks.justenoughcraftingtables.common.block;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.network.NetworkHooks;

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
            NetworkHooks.openScreen(serverPlayer, station, buffer -> StationMenu.writeInitialData(serverPlayer, station, buffer));
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    public static MenuProvider menuProvider(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StationBlockEntity station ? station : null;
    }

    public static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (state.is(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StationBlockEntity station) {
            Containers.dropContents(level, pos, station.takeGridForDrop());
        }
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

    public static void setPlacedName(Level level, BlockPos pos, BlockState state, ItemStack stack) {
        if (state.getBlock() instanceof StationSlabBlock && state.getValue(StationSlabBlock.TYPE) == SlabType.DOUBLE) {
            return;
        }
        if (stack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof StationBlockEntity station) {
            station.setCustomName(stack.getHoverName());
        }
    }

    public static ItemStack cloneStack(Block block, BlockState state) {
        ItemStack stack = new ItemStack(block);
        StationColor.apply(stack, state.getValue(StationColor.PROPERTY));
        return stack;
    }
}
