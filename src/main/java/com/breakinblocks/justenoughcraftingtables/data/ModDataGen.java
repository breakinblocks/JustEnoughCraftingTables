package com.breakinblocks.justenoughcraftingtables.data;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JustEnoughCraftingTables.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModDataGen {
    private ModDataGen() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeClient(), new StationBlockStates(generator.getPackOutput(), event.getExistingFileHelper()));
    }
}
