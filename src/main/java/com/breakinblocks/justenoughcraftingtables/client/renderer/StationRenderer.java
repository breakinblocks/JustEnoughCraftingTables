package com.breakinblocks.justenoughcraftingtables.client.renderer;

import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class StationRenderer implements BlockEntityRenderer<StationBlockEntity, StationRenderer.State> {
    private static final float CELL = 3.0F / 16.0F;
    private final ItemModelResolver itemModels;

    public static final class State extends BlockEntityRenderState {
        final ItemStackRenderState[] items = new ItemStackRenderState[StationBlockEntity.GRID_SIZE];
        Direction facing = Direction.NORTH;
        float surface;
        boolean visible;

        State() {
            for (int i = 0; i < items.length; i++) items[i] = new ItemStackRenderState();
        }
    }

    public StationRenderer(BlockEntityRendererProvider.Context context) {
        itemModels = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StationBlockEntity station, State state, float partialTick, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(station, state, partialTick, cameraPosition, breakProgress);
        for (ItemStackRenderState item : state.items) item.clear();
        state.visible = JustEnoughCraftingTablesConfig.CLIENT.renderGridItems.get() && station.getLevel() != null && !station.isGridEmpty();
        if (!state.visible) return;
        BlockState blockState = station.getBlockState();
        state.facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.surface = 1.0F;
        if (blockState.getBlock() instanceof StationSlabBlock) {
            state.facing = state.facing.getOpposite();
            if (blockState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM) state.surface = 0.5F;
        }
        state.lightCoords = LevelRenderer.getLightCoords(station.getLevel(), station.getBlockPos().above());
        int seed = (int) station.getBlockPos().asLong();
        for (int slot = 0; slot < state.items.length; slot++) {
            itemModels.updateForTopItem(state.items[slot], station.getGridItem(slot), ItemDisplayContext.FIXED, station.getLevel(), null, seed + slot);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) return;
        pose.pushPose();
        pose.translate(0.5F, state.surface, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        for (int slot = 0; slot < state.items.length; slot++) {
            ItemStackRenderState item = state.items[slot];
            if (item.isEmpty()) continue;
            boolean block = item.usesBlockLight();
            float scale = block ? 0.25F : 0.175F;
            pose.pushPose();
            pose.translate((slot % 3 - 1) * CELL, block ? 1.0F / 16.0F : 0.005F, (slot / 3 - 1) * CELL);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.scale(scale, scale, scale);
            item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        pose.popPose();
    }
}
