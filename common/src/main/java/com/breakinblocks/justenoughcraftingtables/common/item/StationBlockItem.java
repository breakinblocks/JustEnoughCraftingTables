package com.breakinblocks.justenoughcraftingtables.common.item;

import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class StationBlockItem extends BlockItem {
    public StationBlockItem(Block block, Properties properties) {
        super(block, properties.useBlockDescriptionPrefix().cookingFuel(block instanceof StationSlabBlock
                ? ContextIntProviders.COOKING_TIME_WOOD_SLABS : ContextIntProviders.COOKING_TIME_WOOD_BLOCKS));
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        var stack = context.getItemInHand();
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof StationBlockEntity station
                && !station.isGridEmpty()) {
            var data = stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            try {
                return super.place(context);
            } finally {
                if (data != null) stack.set(DataComponents.BLOCK_ENTITY_DATA, data);
            }
        }
        return super.place(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        StationColor color = StationColor.of(stack);
        if (color.dye() != null) {
            tooltipComponents.accept(Component.translatable("color.minecraft." + color.dye().getName()).withStyle(ChatFormatting.GRAY));
        }
    }
}
