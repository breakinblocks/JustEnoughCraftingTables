package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.menu.Source;
import com.breakinblocks.justenoughcraftingtables.menu.SourceOrder;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@GameTestHolder(JustEnoughCraftingTables.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ForgeBackportTests {
    private static final class SwitchingChest extends ChestBlockEntity {
        private ItemStackHandler handler = new ItemStackHandler(1);
        private LazyOptional<IItemHandler> items = LazyOptional.of(() -> handler);

        private SwitchingChest(BlockPos pos) {
            super(pos, Blocks.CHEST.defaultBlockState());
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
            return capability == ForgeCapabilities.ITEM_HANDLER ? items.cast() : super.getCapability(capability, side);
        }

        private void replace() {
            items.invalidate();
            handler = new ItemStackHandler(1);
            items = LazyOptional.of(() -> handler);
        }
    }

    @GameTest(template = "empty")
    public static void invalidatedCapabilityCannotBeReused(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.CRAFTING_STATION.get());
        helper.setBlock(pos.north(), Blocks.CHEST);
        SwitchingChest chest = new SwitchingChest(helper.absolutePos(pos.north()));
        helper.getLevel().setBlockEntity(chest);
        chest.handler.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
        ItemStackHandler previous = chest.handler;
        StationBlockEntity station = (StationBlockEntity) helper.getBlockEntity(pos);
        ServerPlayer player = TestSupport.serverPlayer(helper);
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(pos.south())));
        StationMenu menu = new StationMenu(7, player.getInventory(), station);
        Source source = menu.sources(SourceOrder.CONNECTED_FIRST).get(0);
        chest.replace();
        chest.handler.setStackInSlot(0, new ItemStack(Items.EMERALD, 2));
        menu.broadcastChanges();
        helper.assertTrue(source.handler().extractItem(0, 1, false).isEmpty(), "Retained reference used an invalidated capability");
        helper.assertTrue(menu.sources(SourceOrder.CONNECTED_FIRST).get(0).handler().extractItem(0, 1, false).is(Items.EMERALD),
                "Replacement capability was not discovered");
        helper.assertTrue(previous.getStackInSlot(0).getCount() == 3 && chest.handler.getStackInSlot(0).getCount() == 1,
                "Capability replacement changed the wrong inventory");
        menu.removed(player);
        helper.succeed();
    }

}
