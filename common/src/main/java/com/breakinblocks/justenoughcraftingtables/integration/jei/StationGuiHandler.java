package com.breakinblocks.justenoughcraftingtables.integration.jei;

import com.breakinblocks.justenoughcraftingtables.client.gui.StationScreen;
import java.util.Collection;
import java.util.List;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

public class StationGuiHandler implements IGuiContainerHandler<StationScreen> {
    public static final int ARROW_X = 90;
    public static final int ARROW_Y = 35;
    public static final int ARROW_WIDTH = 22;
    public static final int ARROW_HEIGHT = 15;

    @Override
    public List<Rect2i> getGuiExtraAreas(StationScreen screen) {
        return screen.getExclusionAreas();
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(StationScreen screen, double guiMouseX, double guiMouseY) {
        return List.of(IGuiClickableArea.createBasic(ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, RecipeTypes.CRAFTING));
    }
}
