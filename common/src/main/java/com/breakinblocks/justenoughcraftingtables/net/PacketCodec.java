package com.breakinblocks.justenoughcraftingtables.net;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;

public interface PacketCodec<T> {
    T decode(FriendlyByteBuf buffer);
    void encode(FriendlyByteBuf buffer, T value);

    static <T> PacketCodec<T> of(BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        return new PacketCodec<>() {
            @Override
            public T decode(FriendlyByteBuf buffer) { return reader.apply(buffer); }
            @Override
            public void encode(FriendlyByteBuf buffer, T value) { writer.accept(buffer, value); }
        };
    }

    default PacketCodec<Optional<T>> optional() {
        return of((buffer, value) -> {
            buffer.writeBoolean(value.isPresent());
            value.ifPresent(present -> encode(buffer, present));
        }, buffer -> buffer.readBoolean() ? Optional.of(decode(buffer)) : Optional.empty());
    }
}
