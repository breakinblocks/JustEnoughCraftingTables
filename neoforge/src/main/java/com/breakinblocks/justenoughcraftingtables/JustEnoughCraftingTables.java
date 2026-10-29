package com.breakinblocks.justenoughcraftingtables;

import com.breakinblocks.justenoughcraftingtables.client.JustEnoughCraftingTablesClient;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.breakinblocks.justenoughcraftingtables.registry.ModItems;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import com.breakinblocks.justenoughcraftingtables.registry.ModRecipeSerializers;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(JustEnoughCraftingTables.MOD_ID)
public class JustEnoughCraftingTables {
    public static final String MOD_ID = "justenoughcraftingtables";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public JustEnoughCraftingTables(IEventBus eventBus, ModContainer container, Dist dist) {
        ModBlocks.BLOCKS.register(eventBus);
        ModItems.ITEMS.register(eventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(eventBus);
        ModMenus.MENUS.register(eventBus);
        ModRecipeSerializers.SERIALIZERS.register(eventBus);
        container.registerConfig(ModConfig.Type.SERVER, JustEnoughCraftingTablesConfig.serverSpec);
        if (dist.isClient()) {
            container.registerConfig(ModConfig.Type.CLIENT, JustEnoughCraftingTablesConfig.clientSpec);
            JustEnoughCraftingTablesClient.init(container);
        }
    }
}
