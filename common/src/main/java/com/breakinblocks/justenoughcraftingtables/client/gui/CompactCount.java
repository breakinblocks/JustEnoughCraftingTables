package com.breakinblocks.justenoughcraftingtables.client.gui;

public final class CompactCount {
    private static final String[] UNITS = {"k", "M", "B", "T", "P"};

    private CompactCount() {
    }

    public static String format(long count) {
        if (count < 1000) {
            return Long.toString(count);
        }
        double value = count;
        int unit = -1;
        while (value >= 1000 && unit < UNITS.length - 1) {
            value /= 1000;
            unit++;
        }
        if (value < 10) {
            long tenths = (long) Math.floor(value * 10);
            return tenths % 10 == 0 ? (tenths / 10) + UNITS[unit] : (tenths / 10) + "." + (tenths % 10) + UNITS[unit];
        }
        return (long) Math.floor(value) + UNITS[unit];
    }
}
