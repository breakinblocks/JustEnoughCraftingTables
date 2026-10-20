package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.menu.SideEntry;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SidesPayload(int containerId, int version, List<SideEntry> sides, Optional<Direction> selected, boolean viewReset)
        implements CustomPacketPayload {
    public static final Type<SidesPayload> TYPE = new Type<>(JustEnoughCraftingTables.id("sides"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SidesPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SidesPayload::containerId,
            ByteBufCodecs.VAR_INT, SidesPayload::version,
            SideEntry.LIST_STREAM_CODEC, SidesPayload::sides,
            NetCodecs.OPTIONAL_DIRECTION, SidesPayload::selected,
            ByteBufCodecs.BOOL, SidesPayload::viewReset,
            SidesPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
