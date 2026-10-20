package com.breakinblocks.justenoughcraftingtables.common.block;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

public enum StationColor implements StringRepresentable {
    NONE("none", null),
    WHITE("white", DyeColor.WHITE),
    ORANGE("orange", DyeColor.ORANGE),
    MAGENTA("magenta", DyeColor.MAGENTA),
    LIGHT_BLUE("light_blue", DyeColor.LIGHT_BLUE),
    YELLOW("yellow", DyeColor.YELLOW),
    LIME("lime", DyeColor.LIME),
    PINK("pink", DyeColor.PINK),
    GRAY("gray", DyeColor.GRAY),
    LIGHT_GRAY("light_gray", DyeColor.LIGHT_GRAY),
    CYAN("cyan", DyeColor.CYAN),
    PURPLE("purple", DyeColor.PURPLE),
    BLUE("blue", DyeColor.BLUE),
    BROWN("brown", DyeColor.BROWN),
    GREEN("green", DyeColor.GREEN),
    RED("red", DyeColor.RED),
    BLACK("black", DyeColor.BLACK);

    public static final EnumProperty<StationColor> PROPERTY = EnumProperty.create("color", StationColor.class);

    private final String name;

    @Nullable
    private final DyeColor dye;

    StationColor(String name, @Nullable DyeColor dye) {
        this.name = name;
        this.dye = dye;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Nullable
    public DyeColor dye() {
        return dye;
    }

    public boolean isDyed() {
        return dye != null;
    }

    public int tint() {
        return dye == null ? 0xFFFFFFFF : dye.getTextureDiffuseColor();
    }

    public static StationColor of(@Nullable DyeColor dye) {
        if (dye == null) {
            return NONE;
        }
        for (StationColor color : values()) {
            if (color.dye == dye) {
                return color;
            }
        }
        return NONE;
    }

    public static StationColor of(ItemStack stack) {
        BlockItemStateProperties properties = stack.get(DataComponents.BLOCK_STATE);
        if (properties == null) {
            return NONE;
        }
        StationColor color = properties.get(PROPERTY);
        return color == null ? NONE : color;
    }

    public static ItemStack apply(ItemStack stack, StationColor color) {
        BlockItemStateProperties properties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
        Map<String, String> map = new HashMap<>(properties.properties());
        if (color == NONE) {
            map.remove(PROPERTY.getName());
        } else {
            map.put(PROPERTY.getName(), color.getSerializedName());
        }
        if (map.isEmpty()) {
            stack.remove(DataComponents.BLOCK_STATE);
        } else {
            stack.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(Map.copyOf(map)));
        }
        return stack;
    }
}
