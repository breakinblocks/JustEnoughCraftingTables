package com.breakinblocks.justenoughcraftingtables.data;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class StationBlockStates extends BlockStateProvider {
    public StationBlockStates(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, JustEnoughCraftingTables.MOD_ID, existingFileHelper);
    }

    private static ModelFile model(String name) {
        return new ModelFile.UncheckedModelFile(JustEnoughCraftingTables.id("block/" + name));
    }

    private static int rotation(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile station = model("crafting_station");
        ModelFile stationDyed = model("crafting_station_dyed");
        getVariantBuilder(ModBlocks.CRAFTING_STATION.get()).forAllStates(state -> {
            boolean dyed = state.getValue(StationColor.PROPERTY).isDyed();
            return ConfiguredModel.builder()
                    .modelFile(dyed ? stationDyed : station)
                    .rotationY(rotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                    .build();
        });
        ModelFile bottom = model("crafting_station_slab");
        ModelFile top = model("crafting_station_slab_top");
        ModelFile full = model("crafting_station_slab_double");
        ModelFile bottomDyed = model("crafting_station_slab_dyed");
        ModelFile topDyed = model("crafting_station_slab_top_dyed");
        ModelFile fullDyed = model("crafting_station_slab_double_dyed");
        getVariantBuilder(ModBlocks.CRAFTING_STATION_SLAB.get()).forAllStatesExcept(state -> {
            boolean dyed = state.getValue(StationColor.PROPERTY).isDyed();
            SlabType type = state.getValue(BlockStateProperties.SLAB_TYPE);
            ModelFile file = switch (type) {
                case BOTTOM -> dyed ? bottomDyed : bottom;
                case TOP -> dyed ? topDyed : top;
                case DOUBLE -> dyed ? fullDyed : full;
            };
            return ConfiguredModel.builder()
                    .modelFile(file)
                    .rotationY(rotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                    .build();
        }, BlockStateProperties.WATERLOGGED);
    }
}
