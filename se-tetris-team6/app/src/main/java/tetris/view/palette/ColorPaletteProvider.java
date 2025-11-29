package tetris.view.palette;

import java.awt.Color;

/**
 * Provides block color palettes depending on accessibility options.
 */
public final class ColorPaletteProvider {

    private static final Color[] STANDARD_PALETTE = {
        new Color(30, 30, 30),
        new Color(0, 240, 240),
        new Color(0, 0, 240),
        new Color(240, 160, 0),
        new Color(240, 240, 0),
        new Color(0, 240, 0),
        new Color(160, 0, 240),
        new Color(240, 0, 0)
    };

    // Color-blind-friendly palette (high contrast, prot/deut safe)
    private static final Color[] COLORBLIND_PALETTE = {
        new Color(30, 30, 30), // empty
        new Color(0, 180, 255), // I
        new Color(34, 139, 34), // J
        new Color(255, 140, 0), // L
        new Color(255, 215, 0), // O
        new Color(128, 0, 128), // S
        new Color(255, 0, 128), // T
        new Color(220, 20, 60) // Z
    };

    private ColorPaletteProvider() {
    }

    public static Color[] palette(boolean colorBlindMode) {
        return colorBlindMode ? COLORBLIND_PALETTE : STANDARD_PALETTE;
    }
}
