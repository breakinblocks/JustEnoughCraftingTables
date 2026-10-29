package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.platform.Platform;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;

/** Exercises actual menu opening, rendering, and client/server slot synchronization. */
public final class StationClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getClientLevel().waitForChunksDownload();
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                var pos = player.blockPosition().east();
                level.setBlockAndUpdate(pos, ModBlocks.CRAFTING_STATION.get().defaultBlockState()
                        .setValue(StationColor.PROPERTY, StationColor.of(DyeColor.BLUE)));
                level.setBlockAndUpdate(pos.north(), Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.TYPE, ChestType.LEFT));
                level.setBlockAndUpdate(pos.north().east(), Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.TYPE, ChestType.RIGHT));
                var chest = ChestBlock.getContainer((ChestBlock) Blocks.CHEST,
                        level.getBlockState(pos.north()), level, pos.north(), true);
                if (chest == null || chest.getContainerSize() != 54) {
                    throw new AssertionError("Test setup did not create a double chest");
                }
                chest.setItem(0, new ItemStack(Items.DIAMOND, 12));
                Platform.openMenu(player, (StationBlockEntity) level.getBlockEntity(pos));
            });
            context.waitForScreen(StationScreen.class);
            context.waitFor(client -> client.player.containerMenu instanceof StationMenu menu
                    && menu.windowSlot(0).getItem().is(Items.DIAMOND));
            for (int tick = 0; tick < 60; tick++) {
                context.waitTick();
                context.runOnClient(client -> {
                    var menu = (StationMenu) client.player.containerMenu;
                    if (!menu.windowSlot(0).getItem().is(Items.DIAMOND)) {
                        throw new AssertionError("Double-chest panel lost its contents while open");
                    }
                });
            }
            context.takeScreenshot("fabric-station");
            context.runOnClient(client -> client.gameMode.handleContainerInput(client.player.containerMenu.containerId,
                    StationMenu.WINDOW_START, 0, ContainerInput.PICKUP, client.player));
            context.waitFor(client -> client.player.containerMenu.getCarried().getCount() == 12);
            // The client predicts the pickup before the server processes its packet.
            for (int tick = 0; tick < 200; tick++) {
                if (world.getServer().computeOnServer(server -> server.getPlayerList().getPlayers().getFirst()
                        .containerMenu.getCarried().getCount() == 12)) break;
                context.waitTick();
            }
            world.getServer().runOnServer(server -> {
                var menu = server.getPlayerList().getPlayers().getFirst().containerMenu;
                if (!menu.getCarried().is(Items.DIAMOND) || menu.getCarried().getCount() != 12) {
                    throw new AssertionError("Client pickup did not reach the server");
                }
            });
        }
    }
}
