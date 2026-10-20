package com.breakinblocks.justenoughcraftingtables.client.gui;

import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class IconButton extends AbstractButton {
    public record Sprites(Identifier normal, Identifier highlighted, @Nullable Identifier disabled) {
    }

    private final Supplier<Sprites> sprites;
    private final Supplier<Component> label;
    private final Runnable action;

    @Nullable
    private Component lastLabel;

    public IconButton(int x, int y, Supplier<Sprites> sprites, Supplier<Component> label, Runnable action) {
        super(x, y, 12, 12, label.get());
        this.sprites = sprites;
        this.label = label;
        this.action = action;
        refreshLabel();
    }

    public void refreshLabel() {
        Component current = label.get();
        if (!current.equals(lastLabel)) {
            lastLabel = current;
            setMessage(current);
            setTooltip(Tooltip.create(current));
        }
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Sprites set = sprites.get();
        Identifier sprite;
        if (!active && set.disabled() != null) {
            sprite = set.disabled();
        } else if (active && isHoveredOrFocused()) {
            sprite = set.highlighted();
        } else {
            sprite = set.normal();
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), width, height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
