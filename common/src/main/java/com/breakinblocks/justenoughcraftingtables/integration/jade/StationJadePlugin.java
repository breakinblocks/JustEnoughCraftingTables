package com.breakinblocks.justenoughcraftingtables.integration.jade;

import com.breakinblocks.justenoughcraftingtables.JustEnoughCraftingTables;
import com.breakinblocks.justenoughcraftingtables.common.block.StationBlock;
import com.breakinblocks.justenoughcraftingtables.common.block.StationSlabBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(JustEnoughCraftingTables.MOD_ID)
public class StationJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(StationJadeProvider.INSTANCE, StationBlock.class);
        registration.registerBlockComponent(StationJadeProvider.INSTANCE, StationSlabBlock.class);
    }
}
