package com.breakinblocks.justenoughcraftingtables.net;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class NetCodecs {
    public static final int MAX_IDS = 65536;

    public static final StreamCodec<ByteBuf, Optional<Direction>> OPTIONAL_DIRECTION = ByteBufCodecs.optional(Direction.STREAM_CODEC);

    public static final StreamCodec<ByteBuf, int[]> SORTED_IDS = new StreamCodec<>() {
        @Override
        public int[] decode(ByteBuf buffer) {
            int size = VarInt.read(buffer);
            if (size < 0 || size > MAX_IDS || size > buffer.readableBytes()) {
                throw new IllegalArgumentException("Too many ids: " + size);
            }
            int[] ids = new int[size];
            int previous = -1;
            for (int i = 0; i < size; i++) {
                int delta = VarInt.read(buffer);
                long next = (long) previous + delta + 1;
                if (delta < 0 || next > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Invalid sorted id delta");
                }
                previous = (int) next;
                ids[i] = previous;
            }
            return ids;
        }

        @Override
        public void encode(ByteBuf buffer, int[] value) {
            if (value.length > MAX_IDS) {
                throw new IllegalArgumentException("Too many ids: " + value.length);
            }
            int[] sorted = value.clone();
            Arrays.sort(sorted);
            int count = 0;
            for (int i = 0; i < sorted.length; i++) {
                if (sorted[i] >= 0 && (i == 0 || sorted[i] != sorted[i - 1])) {
                    sorted[count++] = sorted[i];
                }
            }
            VarInt.write(buffer, count);
            int previous = -1;
            for (int i = 0; i < count; i++) {
                VarInt.write(buffer, sorted[i] - previous - 1);
                previous = sorted[i];
            }
        }
    };

    public static final StreamCodec<ByteBuf, int[]> SIGNED_INTS = new StreamCodec<>() {
        @Override
        public int[] decode(ByteBuf buffer) {
            int size = VarInt.read(buffer);
            if (size < 0 || size > 64) {
                throw new IllegalArgumentException("Too many entries: " + size);
            }
            int[] values = new int[size];
            for (int i = 0; i < size; i++) {
                values[i] = VarInt.read(buffer) - 1;
            }
            return values;
        }

        @Override
        public void encode(ByteBuf buffer, int[] value) {
            VarInt.write(buffer, value.length);
            for (int v : value) {
                VarInt.write(buffer, v + 1);
            }
        }
    };

    private NetCodecs() {
    }
}
