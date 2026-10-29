package com.breakinblocks.justenoughcraftingtables.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class GuiSprites {
    private static final ResourceLocation CREATIVE_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    private GuiSprites() {
    }

    static void blit(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if (sprite.getNamespace().equals("minecraft") && sprite.getPath().equals("container/creative_inventory/scroller")) {
            graphics.blit(CREATIVE_TABS, x, y, 232, 0, width, height);
        } else {
            ResourceLocation texture = new ResourceLocation(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
            graphics.blit(texture, x, y, 0, 0, width, height, width, height);
        }
    }
}
