package com.breakinblocks.justenoughcraftingtables.gametest;

import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.menu.StationMenu;
import com.breakinblocks.justenoughcraftingtables.net.NetCodecs;
import com.breakinblocks.justenoughcraftingtables.net.ViewFragmentPayload;
import com.breakinblocks.justenoughcraftingtables.net.ViewPayload;
import com.breakinblocks.justenoughcraftingtables.net.ViewRequestAssembler;
import com.breakinblocks.justenoughcraftingtables.net.ViewStatePayload;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class BackportTests {
    public static void largeSearchFitsLegacyPacketLimit(GameTestHelper helper) {
        int[] ids = new int[NetCodecs.MAX_IDS];
        for (int i = 0; i < ids.length; i++) ids[i] = i * 32767;
        ViewPayload original = new ViewPayload(7, 19, 2,
                new ViewPayload.Request(Optional.of(Direction.NORTH), 0, 0, Optional.of(ids), true));
        ViewRequestAssembler assembler = new ViewRequestAssembler();
        ViewPayload complete = null;
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (ViewFragmentPayload fragment : ViewFragmentPayload.split(original)) {
                buffer.clear();
                ViewFragmentPayload.STREAM_CODEC.encode(buffer, fragment);
                helper.assertTrue(buffer.readableBytes() + 1 <= 32767, "Search fragment exceeds legacy packet limit");
                complete = assembler.accept(ViewFragmentPayload.STREAM_CODEC.decode(buffer));
                helper.assertTrue(buffer.readableBytes() == 0, "Trailing search fragment data");
            }
        } finally {
            buffer.release();
        }
        helper.assertTrue(complete != null && complete.requestId() == original.requestId()
                && Arrays.equals(complete.request().filter().orElseThrow(), ids), "Large search was not reconstructed");
        helper.succeed();
    }

    public static void interleavedSearchFragmentsCannotMixRequests(GameTestHelper helper) {
        int[] ids = new int[5000];
        Arrays.setAll(ids, i -> i);
        ViewPayload.Request request = new ViewPayload.Request(Optional.of(Direction.NORTH), 0, 0, Optional.of(ids), true);
        List<ViewFragmentPayload> first = ViewFragmentPayload.split(new ViewPayload(7, 1, 2, request));
        List<ViewFragmentPayload> second = ViewFragmentPayload.split(new ViewPayload(7, 2, 2, request));
        ViewRequestAssembler assembler = new ViewRequestAssembler();
        helper.assertTrue(assembler.accept(first.get(0)) == null, "Incomplete search was applied");
        helper.assertTrue(assembler.accept(second.get(1)) == null, "Different requests were mixed");
        helper.assertTrue(assembler.accept(first.get(1)) == null, "Rejected search retained partial state");
        helper.assertTrue(assembler.accept(second.get(0)) == null && assembler.accept(second.get(1)) != null,
                "Fresh search could not recover from invalid fragments");
        helper.succeed();
    }

    public static void oversizedItemCodecPreservesCountAndTag(GameTestHelper helper) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (int count : new int[] {1, 127, 128, 256, 10000, Integer.MAX_VALUE}) {
                ItemStack original = new ItemStack(Items.DIAMOND, count);
                original.setHoverName(Component.literal("Stored diamonds"));
                NetCodecs.ITEM.encode(buffer, original);
                helper.assertTrue(ItemStack.matches(original, NetCodecs.ITEM.decode(buffer)), "Stack changed at count " + count);
            }
            helper.assertTrue(buffer.readableBytes() == 0, "Trailing item data");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    public static void vanillaSyncCannotOverwriteStorageCounts(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.CRAFTING_STATION.get());
        helper.setBlock(pos.north(), Blocks.CHEST);
        StationBlockEntity station = (StationBlockEntity) helper.getBlockEntity(pos);
        ServerPlayer player = TestSupport.serverPlayer(helper);
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(pos.south())));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            StationMenu.writeInitialData(player, station, buffer);
            StationMenu menu = new StationMenu(7, player.getInventory(), buffer);
            helper.assertTrue(buffer.readableBytes() == 0 && menu.selectedSide() == Direction.NORTH, "Open data did not reconstruct selected storage");
            ViewStatePayload state = new ViewStatePayload(7, 0,
                    new ViewStatePayload.Info(Optional.of(Direction.NORTH), 27, 1, 0, 0, false),
                    new int[] {0}, List.of(new ItemStack(Items.DIAMOND, 10000)));
            ViewStatePayload.STREAM_CODEC.encode(buffer, state);
            menu.applyViewState(ViewStatePayload.STREAM_CODEC.decode(buffer));
            menu.setItem(StationMenu.WINDOW_START, 3, new ItemStack(Items.DIAMOND, 16));
            helper.assertTrue(menu.windowSlot(0).getItem().getCount() == 10000, "Single slot packet truncated storage");
            List<ItemStack> contents = new ArrayList<>();
            for (int i = 0; i < menu.slots.size(); i++) contents.add(ItemStack.EMPTY);
            contents.set(StationMenu.WINDOW_START, new ItemStack(Items.DIAMOND, 16));
            contents.set(StationMenu.INVENTORY_START, new ItemStack(Items.APPLE, 3));
            menu.initializeContents(4, contents, ItemStack.EMPTY);
            helper.assertTrue(menu.windowSlot(0).getItem().getCount() == 10000, "Full menu packet truncated storage");
            helper.assertTrue(menu.getSlot(StationMenu.INVENTORY_START).getItem().getCount() == 3 && menu.getStateId() == 4,
                    "Vanilla inventory/state sync was lost");
            menu.removed(player);
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    public static void malformedSavedNameDoesNotBreakStation(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.CRAFTING_STATION.get());
        StationBlockEntity station = (StationBlockEntity) helper.getBlockEntity(pos);
        CompoundTag tag = new CompoundTag();
        tag.putString("CustomName", "{invalid");
        station.load(tag);
        helper.assertTrue(station.getCustomName() == null && station.isGridEmpty(), "Malformed name damaged station loading");
        helper.succeed();
    }

    public static void placedItemsKeepCustomNames(GameTestHelper helper) {
        ServerPlayer player = TestSupport.serverPlayer(helper);
        for (int i = 0; i < 2; i++) {
            BlockPos pos = helper.absolutePos(new BlockPos(2 + i * 3, 1, 3));
            ItemStack stack = new ItemStack(i == 0 ? ModBlocks.CRAFTING_STATION.get() : ModBlocks.CRAFTING_STATION_SLAB.get());
            stack.setHoverName(Component.literal("Named station " + i));
            player.setPos(Vec3.atBottomCenterOf(pos.south()));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            helper.assertTrue(((BlockItem) stack.getItem()).place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit)).consumesAction(), "Placement failed");
            helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof StationBlockEntity station
                    && station.getDisplayName().getString().equals("Named station " + i), "Placed station lost custom name");
        }
        helper.succeed();
    }
}
