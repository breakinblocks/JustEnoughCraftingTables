package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ViewPayload(int containerId, int requestId, int sidesVersion, Request request) implements CustomPacketPayload {
    public static final Type<ViewPayload> TYPE = new Type<>(JustEnoughCraftingTables.id("view"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ViewPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ViewPayload::containerId,
            ByteBufCodecs.VAR_INT, ViewPayload::requestId,
            ByteBufCodecs.VAR_INT, ViewPayload::sidesVersion,
            Request.STREAM_CODEC, ViewPayload::request,
            ViewPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Request(Optional<Direction> side, int page, int first, Optional<int[]> filter, boolean rebuild) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> STREAM_CODEC = StreamCodec.composite(
                NetCodecs.OPTIONAL_DIRECTION, Request::side,
                ByteBufCodecs.VAR_INT, Request::page,
                ByteBufCodecs.VAR_INT, Request::first,
                ByteBufCodecs.optional(NetCodecs.SORTED_IDS), Request::filter,
                ByteBufCodecs.BOOL, Request::rebuild,
                Request::new);
    }
}
