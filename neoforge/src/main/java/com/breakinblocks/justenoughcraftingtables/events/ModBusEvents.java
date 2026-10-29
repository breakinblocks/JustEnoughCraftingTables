package com.breakinblocks.justenoughcraftingtables.events;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationColor;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCauldronInteractionEvent;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID)
public final class ModBusEvents {
    public static final CauldronInteraction WASH_STATION = (state, level, pos, player, hand, stack) -> {
        if (!StationColor.of(stack).isDyed()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            ItemStack washed = StationColor.apply(stack.copyWithCount(1), StationColor.NONE);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, washed, false));
            player.awardStat(Stats.USE_CAULDRON);
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }
        return InteractionResult.SUCCESS;
    };

    private ModBusEvents() {
    }

    @SubscribeEvent
    public static void registerCauldronInteractions(RegisterCauldronInteractionEvent.Interaction event) {
        event.register(Identifier.withDefaultNamespace("water"), ModBlocks.CRAFTING_STATION.get().asItem(), WASH_STATION);
        event.register(Identifier.withDefaultNamespace("water"), ModBlocks.CRAFTING_STATION_SLAB.get().asItem(), WASH_STATION);
    }
}
