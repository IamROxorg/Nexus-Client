package nexus.zigliix.com.client.gui.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class NexusRenderer {
    private NexusRenderer() {}

    public static void fillRoundRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int radius = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (radius <= 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }

        int radiusSq = radius * radius;
        for (int row = 0; row < h; row++) {
            int inset = 0;
            if (row < radius) {
                int dy = radius - row;
                inset = radius - (int) Math.sqrt(Math.max(0, radiusSq - dy * dy));
            } else if (row >= h - radius) {
                int dy = row - (h - radius - 1);
                inset = radius - (int) Math.sqrt(Math.max(0, radiusSq - dy * dy));
            }
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
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
            int c = withAlpha(NexusTheme.shadow(), alpha);
            g.fill(x + i,     y + h + i - 1, x + w + i, y + h + i, c); // bottom
            g.fill(x + w + i - 1, y + i,     x + w + i, y + h + i, c); // right
        }
    }

    /** Vertical gradient fill. */
    public static void fillGradientV(GuiGraphicsExtractor g, int x, int y, int w, int h, int top, int bottom) {
        g.fillGradient(x, y, x + w, y + h, top, bottom);
    }

    /** Horizontal gradient fill. */
    public static void fillGradientH(GuiGraphicsExtractor g, int x, int y, int w, int h, int left, int right) {
        g.fillGradient(x, y, x + w, y + h, left, right);
    }

    public static void drawOutline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        g.outline(x, y, w, h, color);
    }

    /** Replaces alpha while keeping RGB. */
    public static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    public static void drawString(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
        g.text(font, text, x, y, color, false);
    }

    public static void drawCenteredString(GuiGraphicsExtractor g, Font font, String text, int cx, int y, int color) {
        g.centeredText(font, text, cx, y, color);
    }

    /** Draws a 1 px pixel with alpha preserved. */
    public static void fillPixel(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x, y, x + 1, y + 1, color);
    }

    public static void fillCircle(GuiGraphicsExtractor g, float cx, float cy, float radius, int color) {
        if (radius <= 0.0f) {
            return;
        }
        int minY = (int) Math.floor(cy - radius);
        int maxY = (int) Math.ceil(cy + radius);
        float radiusSq = radius * radius;
        for (int y = minY; y <= maxY; y++) {
            float dy = y + 0.5f - cy;
            float dx = (float) Math.sqrt(Math.max(0.0f, radiusSq - dy * dy));
            int minX = (int) Math.floor(cx - dx);
            int maxX = (int) Math.ceil(cx + dx);
            g.fill(minX, y, maxX, y + 1, color);
        }
    }

    public static void fillSmoothRoundRect(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        fillRoundRect(g, Math.round(x), Math.round(y), Math.round(w), Math.round(h), Math.round(radius), color);
    }

    /** Draws a simple line segment using Minecraft's rectangle fill path. */
    public static void drawSmoothLine(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float thickness, int color) {
        if (thickness <= 0.0f) {
            return;
        }

        float half = thickness * 0.5f;
        int minX = (int) Math.floor(Math.min(x1, x2) - half);
        int maxX = (int) Math.ceil(Math.max(x1, x2) + half);
        int minY = (int) Math.floor(Math.min(y1, y2) - half);
        int maxY = (int) Math.ceil(Math.max(y1, y2) + half);
        g.fill(minX, minY, Math.max(minX + 1, maxX), Math.max(minY + 1, maxY), color);
    }

    /** Pill-shaped on/off toggle. Width=20, Height=8. */
    public static void drawToggle(GuiGraphicsExtractor g, int x, int y, boolean enabled) {
        int tw = 20, th = 8;
        int trackColor = enabled ? NexusTheme.accent() : NexusTheme.separator();
        fillRoundRect(g, x, y, tw, th, th / 2, trackColor);
        int knobX = enabled ? x + tw - th : x;
        int knobColor = enabled ? NexusTheme.text() : NexusTheme.textMuted();
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
