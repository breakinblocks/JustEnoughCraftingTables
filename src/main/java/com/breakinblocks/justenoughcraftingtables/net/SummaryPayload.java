package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record SummaryPayload(int containerId, List<NewEntry> entries, List<Total> totals, int partialMask, int resetMask, int[] removedIds)
        implements CustomPacketPayload {
    public static final Type<SummaryPayload> TYPE = new Type<>(JustEnoughCraftingTables.id("summary"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SummaryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SummaryPayload::containerId,
            NewEntry.STREAM_CODEC.apply(ByteBufCodecs.list(256)), SummaryPayload::entries,
            Total.STREAM_CODEC.apply(ByteBufCodecs.list(2048)), SummaryPayload::totals,
            ByteBufCodecs.VAR_INT, SummaryPayload::partialMask,
            ByteBufCodecs.VAR_INT, SummaryPayload::resetMask,
            NetCodecs.SORTED_IDS, SummaryPayload::removedIds,
            SummaryPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record NewEntry(int id, ItemStack template) {
        public static final StreamCodec<RegistryFriendlyByteBuf, NewEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, NewEntry::id,
                ItemStack.STREAM_CODEC, NewEntry::template,
                NewEntry::new);
    }

    public record Total(int id, int side, long total, long available) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Total> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Total::id,
                ByteBufCodecs.VAR_INT, Total::side,
                ByteBufCodecs.VAR_LONG, Total::total,
                ByteBufCodecs.VAR_LONG, Total::available,
                Total::new);
    }
}
