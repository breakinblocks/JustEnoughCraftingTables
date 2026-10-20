package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModBusEvents {
    public static final CauldronInteraction WASH_STATION = (state, level, pos, player, hand, stack) -> {
        if (!StationColor.of(stack).isDyed()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack washed = StationColor.apply(stack.copyWithCount(1), StationColor.NONE);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, washed, false));
            player.awardStat(Stats.USE_CAULDRON);
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    };

    private ModBusEvents() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CauldronInteraction.WATER.put(ModBlocks.CRAFTING_STATION.get().asItem(), WASH_STATION);
            CauldronInteraction.WATER.put(ModBlocks.CRAFTING_STATION_SLAB.get().asItem(), WASH_STATION);
        });
    }
}
