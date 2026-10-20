package com.breakinblocks.justenoughcraftingtables.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public final class NetCodecs {
    public static final int MAX_IDS = 65536;

    public static final PacketCodec<Direction> DIRECTION = PacketCodec.of((buffer, direction) -> buffer.writeByte(direction.get3DDataValue()), buffer -> {
        int direction = buffer.readUnsignedByte();
        if (direction >= 6) throw new IllegalArgumentException("Invalid direction");
        return Direction.from3DDataValue(direction);
    });
    public static final PacketCodec<Optional<Direction>> OPTIONAL_DIRECTION = DIRECTION.optional();
    public static final PacketCodec<ItemStack> ITEM = PacketCodec.of((buffer, stack) -> {
        buffer.writeBoolean(!stack.isEmpty());
        if (!stack.isEmpty()) {
            buffer.writeVarInt(stack.getCount());
            buffer.writeItem(stack.copyWithCount(1));
        }
    }, buffer -> {
        if (!buffer.readBoolean()) return ItemStack.EMPTY;
        int count = buffer.readVarInt();
        ItemStack stack = buffer.readItem();
        if (count <= 0 || stack.isEmpty()) throw new IllegalArgumentException("Invalid item stack");
        return stack.copyWithCount(count);
    });

    public static <T> PacketCodec<List<T>> list(PacketCodec<T> codec, int maximum) {
        return PacketCodec.of((buffer, values) -> {
            if (values.size() > maximum) throw new IllegalArgumentException("Too many list entries");
            buffer.writeVarInt(values.size());
            for (T value : values) codec.encode(buffer, value);
        }, buffer -> {
            int size = buffer.readVarInt();
            if (size < 0 || size > maximum || size > buffer.readableBytes()) throw new IllegalArgumentException("Invalid list size");
            List<T> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) result.add(codec.decode(buffer));
            return result;
        });
    }

    public static final PacketCodec<int[]> SORTED_IDS = new PacketCodec<>() {
        @Override
        public int[] decode(FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            if (size < 0 || size > MAX_IDS || size > buffer.readableBytes()) {
                throw new IllegalArgumentException("Too many ids: " + size);
            }
            int[] ids = new int[size];
            int previous = -1;
            for (int i = 0; i < size; i++) {
                int delta = buffer.readVarInt();
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
        public void encode(FriendlyByteBuf buffer, int[] value) {
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
            buffer.writeVarInt(count);
            int previous = -1;
            for (int i = 0; i < count; i++) {
                buffer.writeVarInt(sorted[i] - previous - 1);
                previous = sorted[i];
            }
        }
    };

    public static final PacketCodec<int[]> SIGNED_INTS = new PacketCodec<>() {
        @Override
        public int[] decode(FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            if (size < 0 || size > 64) {
                throw new IllegalArgumentException("Too many entries: " + size);
            }
            int[] values = new int[size];
            for (int i = 0; i < size; i++) {
                values[i] = buffer.readVarInt() - 1;
            }
            return values;
        }

        @Override
        public void encode(FriendlyByteBuf buffer, int[] value) {
            buffer.writeVarInt(value.length);
            for (int v : value) {
                buffer.writeVarInt(v + 1);
            }
        }
    };

    private NetCodecs() {
    }
}
