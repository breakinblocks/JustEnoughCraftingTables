package com.breakinblocks.justenoughcraftingtables.menu;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum Origin implements StringRepresentable {
    DOWN("down", Direction.DOWN),
    UP("up", Direction.UP),
    NORTH("north", Direction.NORTH),
    SOUTH("south", Direction.SOUTH),
    WEST("west", Direction.WEST),
    EAST("east", Direction.EAST),
    PLAYER("player", null);

    public static final Codec<Origin> CODEC = StringRepresentable.fromEnum(Origin::values);

    private final String name;

    @Nullable
    private final Direction side;

    Origin(String name, @Nullable Direction side) {
        this.name = name;
        this.side = side;
    }

    @Nullable
    public Direction side() {
        return side;
    }

    public boolean isPlayer() {
        return side == null;
    }

    public static Origin of(Direction direction) {
        return values()[direction.get3DDataValue()];
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
