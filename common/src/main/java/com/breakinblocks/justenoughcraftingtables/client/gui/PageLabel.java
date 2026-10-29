package com.breakinblocks.justenoughcraftingtables.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class PageLabel extends AbstractWidget {
    private int page;
    private int pages;

    public PageLabel(int x, int y) {
        super(x, y, 30, 10, Component.empty());
    }

    public void update(int page, int pages) {
        this.page = page;
        this.pages = pages;
        setMessage(Component.translatable("justenoughcraftingtables.narration.page", page + 1, pages));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        String text = Component.translatable("justenoughcraftingtables.page", page + 1, pages).getString();
        if (font.width(text) > width) {
            text = Integer.toString(page + 1);
        }
        int centre = getX() + width / 2;
        guiGraphics.drawString(font, text, centre - font.width(text) / 2, getY() + 2, 0x404040, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }
}
