package com.breakinblocks.justenoughcraftingtables.net;

import java.util.Optional;
import net.minecraft.core.Direction;

public record ViewPayload(int containerId, int requestId, int sidesVersion, Request request) implements StationPacket {
    public static final PacketCodec<ViewPayload> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
        buffer.writeVarInt(payload.containerId());
        buffer.writeVarInt(payload.requestId());
        buffer.writeVarInt(payload.sidesVersion());
        Request.STREAM_CODEC.encode(buffer, payload.request());
    }, buffer -> new ViewPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), Request.STREAM_CODEC.decode(buffer)));

    public record Request(Optional<Direction> side, int page, int first, Optional<int[]> filter, boolean rebuild) {
        public static final PacketCodec<Request> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
            NetCodecs.OPTIONAL_DIRECTION.encode(buffer, payload.side());
            buffer.writeVarInt(payload.page());
            buffer.writeVarInt(payload.first());
            NetCodecs.SORTED_IDS.optional().encode(buffer, payload.filter());
            buffer.writeBoolean(payload.rebuild());
        }, buffer -> new Request(NetCodecs.OPTIONAL_DIRECTION.decode(buffer), buffer.readVarInt(), buffer.readVarInt(),
                NetCodecs.SORTED_IDS.optional().decode(buffer), buffer.readBoolean()));
    }
}
