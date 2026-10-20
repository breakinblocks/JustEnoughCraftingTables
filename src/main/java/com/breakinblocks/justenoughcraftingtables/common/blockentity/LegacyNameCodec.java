package com.breakinblocks.justenoughcraftingtables.common.blockentity;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

final class LegacyNameCodec implements Codec<Component> {
    static final Codec<Component> CODEC = Codec.withAlternative(new LegacyNameCodec(), ComponentSerialization.CODEC);

    @Override
    public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input).flatMap(json -> {
            try {
                T value = JsonOps.INSTANCE.convertTo(ops, JsonParser.parseString(json));
                return ComponentSerialization.CODEC.parse(ops, value).map(component -> Pair.of(component, ops.empty()));
            } catch (RuntimeException exception) {
                return DataResult.error(() -> "Invalid serialized station name");
            }
        });
    }

    @Override
    public <T> DataResult<T> encode(Component input, DynamicOps<T> ops, T prefix) {
        return ComponentSerialization.CODEC.encodeStart(ops, input)
                .flatMap(value -> ops.mergeToPrimitive(prefix, ops.createString(ops.convertTo(JsonOps.INSTANCE, value).toString())));
    }
}
