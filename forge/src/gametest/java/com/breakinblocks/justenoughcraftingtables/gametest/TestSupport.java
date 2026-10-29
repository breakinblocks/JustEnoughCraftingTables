package com.breakinblocks.justenoughcraftingtables.gametest;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;

public final class TestSupport {
    private TestSupport() {
    }

    public static void assertValueEqual(GameTestHelper helper, Object actual, Object expected, String label) {
        helper.assertTrue(Objects.equals(actual, expected), label + ": expected " + expected + ", got " + actual);
    }

    public static CraftingContainer grid(int width, int height, List<ItemStack> items) {
        AbstractContainerMenu menu = new AbstractContainerMenu(null, 0) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };
        CraftingContainer grid = new TransientCraftingContainer(menu, width, height);
        for (int i = 0; i < items.size(); i++) {
            grid.setItem(i, items.get(i).copy());
        }
        return grid;
    }

    public static ServerPlayer serverPlayer(GameTestHelper helper) {
        ServerPlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "test-player"));
        player.setGameMode(GameType.CREATIVE);
        return player;
    }

    public static Player spectator(GameTestHelper helper) {
        return new Player(helper.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "spectator")) {
            @Override
            public boolean isSpectator() {
                return true;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
    }
}
