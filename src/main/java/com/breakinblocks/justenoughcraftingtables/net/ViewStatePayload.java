package com.breakinblocks.justenoughcraftingtables.net;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public record ViewStatePayload(int containerId, int requestId, Info info, int[] realIndexes, List<ItemStack> stacks) implements StationPacket {
    public static final PacketCodec<ViewStatePayload> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
        buffer.writeVarInt(payload.containerId());
        buffer.writeVarInt(payload.requestId());
        Info.STREAM_CODEC.encode(buffer, payload.info());
        NetCodecs.SIGNED_INTS.encode(buffer, payload.realIndexes());
        NetCodecs.list(NetCodecs.ITEM, 64).encode(buffer, payload.stacks());
    }, buffer -> new ViewStatePayload(buffer.readVarInt(), buffer.readVarInt(), Info.STREAM_CODEC.decode(buffer),
            NetCodecs.SIGNED_INTS.decode(buffer), NetCodecs.list(NetCodecs.ITEM, 64).decode(buffer)));

    public record Info(Optional<Direction> side, int length, int pages, int page, int first, boolean filtered) {
        public static final PacketCodec<Info> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
            NetCodecs.OPTIONAL_DIRECTION.encode(buffer, payload.side());
            buffer.writeVarInt(payload.length());
            buffer.writeVarInt(payload.pages());
            buffer.writeVarInt(payload.page());
            buffer.writeVarInt(payload.first());
            buffer.writeBoolean(payload.filtered());
        }, buffer -> new Info(NetCodecs.OPTIONAL_DIRECTION.decode(buffer), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean()));
    }
}
