package com.breakinblocks.justenoughcraftingtables.net;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record SummaryPayload(int containerId, List<NewEntry> entries, List<Total> totals, int partialMask, int resetMask, int[] removedIds) implements StationPacket {
    public static final PacketCodec<SummaryPayload> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
        buffer.writeVarInt(payload.containerId());
        NetCodecs.list(NewEntry.STREAM_CODEC, 256).encode(buffer, payload.entries());
        NetCodecs.list(Total.STREAM_CODEC, 2048).encode(buffer, payload.totals());
        buffer.writeVarInt(payload.partialMask());
        buffer.writeVarInt(payload.resetMask());
        NetCodecs.SORTED_IDS.encode(buffer, payload.removedIds());
    }, buffer -> new SummaryPayload(buffer.readVarInt(), NetCodecs.list(NewEntry.STREAM_CODEC, 256).decode(buffer),
            NetCodecs.list(Total.STREAM_CODEC, 2048).decode(buffer), buffer.readVarInt(), buffer.readVarInt(), NetCodecs.SORTED_IDS.decode(buffer)));

    public record NewEntry(int id, ItemStack template) {
        public static final PacketCodec<NewEntry> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
            buffer.writeVarInt(payload.id());
            NetCodecs.ITEM.encode(buffer, payload.template());
        }, buffer -> new NewEntry(buffer.readVarInt(), NetCodecs.ITEM.decode(buffer)));
    }

    public record Total(int id, int side, long total, long available) {
        public static final PacketCodec<Total> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
            buffer.writeVarInt(payload.id());
            buffer.writeVarInt(payload.side());
            buffer.writeVarLong(payload.total());
            buffer.writeVarLong(payload.available());
        }, buffer -> new Total(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarLong(), buffer.readVarLong()));
    }
}
