package com.breakinblocks.justenoughcraftingtables.data;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Direction;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class StationBlockStates implements DataProvider {
    private final PackOutput.PathProvider paths;

    public StationBlockStates(PackOutput output) {
        paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.allOf(write(output, "crafting_station", false), write(output, "crafting_station_slab", true));
    }

    private CompletableFuture<?> write(CachedOutput output, String name, boolean slab) {
        JsonObject variants = new JsonObject();
        for (StationColor color : StationColor.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (String type : slab ? new String[] {"bottom", "top", "double"} : new String[] {""}) {
                    String key = "color=" + color.getSerializedName() + ",facing=" + facing.getSerializedName();
                    if (slab) key += ",type=" + type;
                    String model = name + (type.equals("top") || type.equals("double") ? "_" + type : "")
                            + (color.isDyed() ? "_dyed" : "");
                    JsonObject variant = new JsonObject();
                    variant.addProperty("model", JustEnoughCraftingTables.id("block/" + model).toString());
                    int rotation = ((int) facing.toYRot() + 180) % 360;
                    if (rotation != 0) variant.addProperty("y", rotation);
                    variants.add(key, variant);
                }
            }
        }
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return DataProvider.saveStable(output, root, paths.json(JustEnoughCraftingTables.id(name)));
    }

    @Override
    public String getName() {
        return "Crafting station block states";
    }
}
