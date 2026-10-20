package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.menu.SideEntry;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;

public record SidesPayload(int containerId, int version, List<SideEntry> sides, Optional<Direction> selected, boolean viewReset) implements StationPacket {
    public static final PacketCodec<SidesPayload> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
        buffer.writeVarInt(payload.containerId());
        buffer.writeVarInt(payload.version());
        SideEntry.LIST_STREAM_CODEC.encode(buffer, payload.sides());
        NetCodecs.OPTIONAL_DIRECTION.encode(buffer, payload.selected());
        buffer.writeBoolean(payload.viewReset());
    }, buffer -> new SidesPayload(buffer.readVarInt(), buffer.readVarInt(), SideEntry.LIST_STREAM_CODEC.decode(buffer),
            NetCodecs.OPTIONAL_DIRECTION.decode(buffer), buffer.readBoolean()));
}
