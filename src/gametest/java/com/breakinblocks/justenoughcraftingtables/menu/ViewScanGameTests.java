package com.breakinblocks.justenoughcraftingtables.menu;

import com.breakinblocks.justenoughcraftingtables.gametest.TestItemStorage;
import com.breakinblocks.justenoughcraftingtables.net.SummaryPayload;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.List;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ViewScanGameTests {
    public static void largeFilterHonorsScanBudget(GameTestHelper helper) {
        int[] reads = {0};
        TestItemStorage handler = new TestItemStorage(10000) {
            @Override
            public ItemStack getStackInSlot(int slot) {
                reads[0]++;
                return super.getStackInSlot(slot);
            }
        };
        ViewScan scan = new ViewScan(handler, new int[] {0});
        SummaryTracker summary = new SummaryTracker();
        boolean complete = false;
        int total = 0;
        while (!complete) {
            reads[0] = 0;
            complete = scan.advance(summary, 256);
            helper.assertTrue(reads[0] <= 256, "Filter exceeded its per-step budget");
            total += reads[0];
        }
        helper.assertValueEqual(10000, total, "Filter did not finish scanning the inventory");
        helper.succeed();
    }

    public static void summaryCodecPreservesAvailabilityAndRetirements(GameTestHelper helper) {
        SummaryPayload payload = new SummaryPayload(1,
                List.of(new SummaryPayload.NewEntry(10, new ItemStack(Items.DIAMOND))),
                List.of(new SummaryPayload.Total(10, 2, 64, 3)), 4, 2, new int[] {1, 3, 7});
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            SummaryPayload.STREAM_CODEC.encode(buffer, payload);
            SummaryPayload decoded = SummaryPayload.STREAM_CODEC.decode(buffer);
            helper.assertValueEqual(payload.totals(), decoded.totals(), "Availability changed in transit");
            helper.assertTrue(Arrays.equals(decoded.removedIds(), payload.removedIds()), "Retired ids changed in transit");
            helper.assertTrue(ItemStack.matches(decoded.entries().getFirst().template(), payload.entries().getFirst().template()),
                    "Item components changed in transit");
            helper.assertValueEqual(0, buffer.readableBytes(), "Codec left trailing bytes");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }
}
