package com.breakinblocks.justenoughcraftingtables.integration.jade;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.common.blockentity.StationBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

public enum StationJadeProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final Identifier UID = JustEnoughCraftingTables.id("station");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        if (state.hasProperty(StationColor.PROPERTY)) {
            DyeColor dye = state.getValue(StationColor.PROPERTY).dye();
            if (dye != null) {
                tooltip.add(Component.translatable("color.minecraft." + dye.getName()).withStyle(ChatFormatting.GRAY));
            }
        }
        if (accessor.getBlockEntity() instanceof StationBlockEntity station) {
            List<Element> icons = new ArrayList<>();
            for (ItemStack stack : station.grid()) {
                if (!stack.isEmpty()) {
                    icons.add(JadeUI.smallItem(stack));
                }
            }
            if (!icons.isEmpty()) {
                tooltip.add(icons);
            }
        }
    }
}
