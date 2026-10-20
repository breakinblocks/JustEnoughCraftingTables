package com.breakinblocks.justenoughcraftingtables.common.block;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;

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
        if (dye == null) return 0xFFFFFFFF;
        float[] rgb = dye.getTextureDiffuseColors();
        return 0xFF000000 | ((int) (rgb[0] * 255) << 16) | ((int) (rgb[1] * 255) << 8) | (int) (rgb[2] * 255);
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
        CompoundTag tag = stack.getTagElement("BlockStateTag");
        if (tag == null) return NONE;
        String name = tag.getString(PROPERTY.getName());
        for (StationColor color : values()) {
            if (color.getSerializedName().equals(name)) return color;
        }
        return NONE;
    }

    public static ItemStack apply(ItemStack stack, StationColor color) {
        CompoundTag properties = stack.getOrCreateTagElement("BlockStateTag");
        if (color == NONE) {
            properties.remove(PROPERTY.getName());
            if (properties.isEmpty()) stack.removeTagKey("BlockStateTag");
        } else {
            properties.putString(PROPERTY.getName(), color.getSerializedName());
        }
        return stack;
    }
}
