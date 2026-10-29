package com.breakinblocks.justenoughcraftingtables.client.renderer;

import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

public class StationRenderer implements BlockEntityRenderer<StationBlockEntity> {
    private static final float CELL = 3.0F / 16.0F;
    private final ItemRenderer itemRenderer;

    public StationRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(StationBlockEntity station, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!JustEnoughCraftingTablesConfig.CLIENT.renderGridItems.get()) {
            return;
        }
        Level level = station.getLevel();
        if (level == null || station.isGridEmpty()) {
            return;
        }
        BlockState state = station.getBlockState();
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        float surface = 1.0F;
        if (state.getBlock() instanceof StationSlabBlock) {
            facing = facing.getOpposite();
            if (state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM) {
                surface = 0.5F;
            }
        }
        int light = LevelRenderer.getLightColor(level, station.getBlockPos().above());
        int seedBase = (int) station.getBlockPos().asLong();
        poseStack.pushPose();
        poseStack.translate(0.5F, surface, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        for (int slot = 0; slot < StationBlockEntity.GRID_SIZE; slot++) {
            ItemStack stack = station.getGridItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            int col = slot % 3;
            int row = slot / 3;
            int seed = seedBase + slot;
            BakedModel model = itemRenderer.getModel(stack, level, null, seed);
            boolean block = model.isGui3d();
            float scale = block ? 0.25F : 0.175F;
            float lift = block ? 1.0F / 16.0F : 0.005F;
            poseStack.pushPose();
            poseStack.translate((col - 1) * CELL, lift, (row - 1) * CELL);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(scale, scale, scale);
            itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, buffer, light, OverlayTexture.NO_OVERLAY, model);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(StationBlockEntity blockEntity) {
        return false;
    }
}
