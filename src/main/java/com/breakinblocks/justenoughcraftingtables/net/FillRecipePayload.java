package com.breakinblocks.justenoughcraftingtables.net;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record FillRecipePayload(int containerId, List<ItemStack> templates, int sets) implements StationPacket {
    public static final PacketCodec<FillRecipePayload> STREAM_CODEC = PacketCodec.of((buffer, payload) -> {
        buffer.writeVarInt(payload.containerId());
        NetCodecs.list(NetCodecs.ITEM, 9).encode(buffer, payload.templates());
        buffer.writeVarInt(payload.sets());
    }, buffer -> new FillRecipePayload(buffer.readVarInt(), NetCodecs.list(NetCodecs.ITEM, 9).decode(buffer), buffer.readVarInt()));
}
