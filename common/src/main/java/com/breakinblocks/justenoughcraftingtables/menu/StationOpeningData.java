package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import javax.annotation.Nullable;

public record StationOpeningData(BlockPos pos, int pageSize, boolean allowInsert, int sidesVersion,
        List<SideEntry> sides, @Nullable Direction selected) {
    public static final StreamCodec<RegistryFriendlyByteBuf, StationOpeningData> STREAM_CODEC = StreamCodec.of(
        (buffer, data) -> data.write(buffer), StationOpeningData::read);
    public static StationOpeningData read(RegistryFriendlyByteBuf buffer) {
        return new StationOpeningData(buffer.readBlockPos(), buffer.readVarInt(), buffer.readBoolean(), buffer.readVarInt(),
            SideEntry.LIST_STREAM_CODEC.decode(buffer), buffer.readBoolean() ? Direction.from3DDataValue(buffer.readVarInt()) : null);
    }
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(pageSize);
        buffer.writeBoolean(allowInsert);
        buffer.writeVarInt(sidesVersion);
        SideEntry.LIST_STREAM_CODEC.encode(buffer, sides);
        buffer.writeBoolean(selected != null);
        if (selected != null) buffer.writeVarInt(selected.get3DDataValue());
    }
}
