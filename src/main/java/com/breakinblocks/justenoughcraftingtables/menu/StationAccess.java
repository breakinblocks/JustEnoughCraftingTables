package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.events.StationAccessEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.common.NeoForge;

public final class StationAccess {
    private StationAccess() {
    }

    public static boolean allowed(Player player, BlockPos stationPos, BlockEntity inventory) {
        return allowed(player, stationPos, inventory, false);
    }

    static boolean allowed(Player player, BlockPos stationPos, BlockEntity inventory, boolean prepareLoot) {
        Level level = inventory.getLevel();
        if (level == null || level != player.level() || inventory.isRemoved()) {
            return false;
        }
        List<BlockEntity> parts = new ArrayList<>();
        parts.add(inventory);
        BlockState state = inventory.getBlockState();
        if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos partnerPos = inventory.getBlockPos().relative(ChestBlock.getConnectedDirection(state));
            if (!level.isLoaded(partnerPos)) {
                return false;
            }
            BlockEntity partner = level.getBlockEntity(partnerPos);
            if (partner == null || !partner.getBlockState().is(state.getBlock())) {
                return false;
            }
            parts.add(partner);
        }
        // Authorize every physical part before reading any contents or generating loot.
        for (BlockEntity part : parts) {
            if (part.isRemoved()
                    || part instanceof Container container && !container.stillValid(player)
                    || part instanceof BaseContainerBlockEntity container && !container.canOpen(player)
                    || NeoForge.EVENT_BUS.post(new StationAccessEvent(player, stationPos, part)).isCanceled()) {
                return false;
            }
        }
        for (BlockEntity part : parts) {
            if (prepareLoot && part instanceof RandomizableContainerBlockEntity container && container.getLootTable() != null) {
                if (player.isSpectator()) {
                    return false;
                }
                container.unpackLootTable(player);
            }
        }
        return true;
    }
}
