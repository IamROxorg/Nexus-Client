package nexus.zigliix.com.client.gui.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class NexusRenderer {
    private NexusRenderer() {}

    /** Fills a rounded rectangle using a cross + corner-circle approximation. */
    public static void fillRoundRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) { g.fill(x, y, x + w, y + h, color); return; }

        // Cross fill (3 rectangles cover the full body)
        g.fill(x + r, y,         x + w - r, y + h,     color);
        g.fill(x,     y + r,     x + r,     y + h - r, color);
        g.fill(x + w - r, y + r, x + w,     y + h - r, color);

        // Corner pixels via circle equation
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                float dx = r - i - 0.5f, dy = r - j - 0.5f;
                if (dx * dx + dy * dy <= (float) r * r) {
                    g.fill(x + i,         y + j,         x + i + 1,     y + j + 1,     color); // TL
                    g.fill(x + w - i - 1, y + j,         x + w - i,     y + j + 1,     color); // TR
                    g.fill(x + i,         y + h - j - 1, x + i + 1,     y + h - j,     color); // BL
                    g.fill(x + w - i - 1, y + h - j - 1, x + w - i,    y + h - j,     color); // BR
                }
            }
        }
    }

    /** Draws a 2 px wide vertical accent bar. */
    public static void drawAccentBar(GuiGraphicsExtractor g, int x, int y, int h, int color) {
        g.fill(x, y, x + 2, y + h, color);
    }

    /** Draws a 1 px horizontal separator. */
    public static void drawSeparator(GuiGraphicsExtractor g, int x, int y, int w, int color) {
        g.fill(x, y, x + w, y + 1, color);
    }

    /** Soft drop-shadow beneath/right of a rect. */
    public static void drawDropShadow(GuiGraphicsExtractor g, int x, int y, int w, int h, int depth) {
        for (int i = 1; i <= depth; i++) {
            int alpha = (int)(55 * (1.0f - (float) i / (depth + 1)));
            int c = (alpha << 24);
            g.fill(x + i,     y + h + i - 1, x + w + i, y + h + i, c); // bottom
            g.fill(x + w + i - 1, y + i,     x + w + i, y + h + i, c); // right
        }
    }

    /** Vertical gradient fill. */
    public static void fillGradientV(GuiGraphicsExtractor g, int x, int y, int w, int h, int top, int bottom) {
        g.fillGradient(x, y, x + w, y + h, top, bottom);
    }

    public static void drawString(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
        g.text(font, text, x, y, color, false);
    }

    public static void drawCenteredString(GuiGraphicsExtractor g, Font font, String text, int cx, int y, int color) {
        g.centeredText(font, text, cx, y, color);
    }

    /** Pill-shaped on/off toggle. Width=20, Height=8. */
    public static void drawToggle(GuiGraphicsExtractor g, int x, int y, boolean enabled) {
        int tw = 20, th = 8;
        int trackColor = enabled ? NexusTheme.ACCENT : NexusTheme.SEPARATOR;
        fillRoundRect(g, x, y, tw, th, th / 2, trackColor);
        int knobX = enabled ? x + tw - th : x;
        int knobColor = enabled ? NexusTheme.TEXT : NexusTheme.TEXT_MUTED;
        fillRoundRect(g, knobX, y, th, th, th / 2, knobColor);
    }

    /** Linear interpolation between two ARGB colours. t in [0,1]. */
    public static int lerpColor(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        return ((int)(a1 + (a2 - a1) * t) << 24)
             | ((int)(r1 + (r2 - r1) * t) << 16)
             | ((int)(g1 + (g2 - g1) * t) << 8)
             |  (int)(b1 + (b2 - b1) * t);
    }
}
