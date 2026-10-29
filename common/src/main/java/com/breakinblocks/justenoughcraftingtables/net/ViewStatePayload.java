package com.breakinblocks.justenoughcraftingtables.net;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record ViewStatePayload(int containerId, int requestId, Info info, int[] realIndexes, List<ItemStack> stacks)
        implements CustomPacketPayload {
    public static final Type<ViewStatePayload> TYPE = new Type<>(JustEnoughCraftingTables.id("view_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ViewStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ViewStatePayload::containerId,
            ByteBufCodecs.VAR_INT, ViewStatePayload::requestId,
            Info.STREAM_CODEC, ViewStatePayload::info,
            NetCodecs.SIGNED_INTS, ViewStatePayload::realIndexes,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(64)), ViewStatePayload::stacks,
            ViewStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Info(Optional<Direction> side, int length, int pages, int page, int first, boolean filtered) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Info> STREAM_CODEC = StreamCodec.composite(
                NetCodecs.OPTIONAL_DIRECTION, Info::side,
                ByteBufCodecs.VAR_INT, Info::length,
                ByteBufCodecs.VAR_INT, Info::pages,
                ByteBufCodecs.VAR_INT, Info::page,
                ByteBufCodecs.VAR_INT, Info::first,
                ByteBufCodecs.BOOL, Info::filtered,
                Info::new);
    }
}
