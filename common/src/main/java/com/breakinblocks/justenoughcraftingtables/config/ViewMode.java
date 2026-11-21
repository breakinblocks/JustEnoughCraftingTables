package com.breakinblocks.justenoughcraftingtables.config;

public enum ViewMode {
    SCROLL,
    PAGES,
    BOTH;

    public ViewMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
