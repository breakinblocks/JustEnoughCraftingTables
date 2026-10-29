package com.breakinblocks.justenoughcraftingtables;

import com.breakinblocks.justenoughcraftingtables.client.JustEnoughCraftingTablesClient;
import com.breakinblocks.justenoughcraftingtables.config.JustEnoughCraftingTablesConfig;
import com.breakinblocks.justenoughcraftingtables.net.ModNetwork;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlockEntities;
import com.breakinblocks.justenoughcraftingtables.registry.ModBlocks;
import com.breakinblocks.justenoughcraftingtables.registry.ModItems;
import com.breakinblocks.justenoughcraftingtables.registry.ModMenus;
import com.breakinblocks.justenoughcraftingtables.registry.ModRecipeSerializers;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(JustEnoughCraftingTables.MOD_ID)
public class JustEnoughCraftingTables {
    public static final String MOD_ID = "justenoughcraftingtables";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public JustEnoughCraftingTables() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext context = ModLoadingContext.get();
        ModNetwork.register();
        if (ModList.get().isLoaded("craftingstation")) {
            throw new IllegalStateException("Just Enough Crafting Tables replaces Crafting Station; remove the Crafting Station jar.");
        }
        ModBlocks.BLOCKS.register(eventBus);
        ModItems.ITEMS.register(eventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(eventBus);
        ModMenus.MENUS.register(eventBus);
        ModRecipeSerializers.SERIALIZERS.register(eventBus);
        context.registerConfig(ModConfig.Type.SERVER, JustEnoughCraftingTablesConfig.serverSpec);
        if (FMLEnvironment.dist.isClient()) {
            context.registerConfig(ModConfig.Type.CLIENT, JustEnoughCraftingTablesConfig.clientSpec);
            JustEnoughCraftingTablesClient.init(context);
        }
    }
}
