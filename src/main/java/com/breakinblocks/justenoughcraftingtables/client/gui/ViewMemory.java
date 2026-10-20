package com.breakinblocks.justenoughcraftingtables.client.gui;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ViewMemory {
    private record Key(ResourceKey<Level> dimension, BlockPos pos) {
    }

    public static final class Station {
        private String query = "";
        private final Map<Direction, int[]> positions = new EnumMap<>(Direction.class);

        public String query() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public int page(Direction side) {
            int[] position = positions.get(side);
            return position == null ? 0 : position[0];
        }

        public int first(Direction side) {
            int[] position = positions.get(side);
            return position == null ? 0 : position[1];
        }

        public void remember(Direction side, int page, int first) {
            positions.put(side, new int[] {page, first});
        }
    }

    private static final Map<Key, Station> STATIONS = new HashMap<>();

    private ViewMemory() {
    }

    public static Station get(ResourceKey<Level> dimension, BlockPos pos) {
        return STATIONS.computeIfAbsent(new Key(dimension, pos.immutable()), key -> new Station());
    }

    public static void clear() {
        STATIONS.clear();
    }
}
