package nexus.zigliix.com.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.NexusTheme;
import nexus.zigliix.com.client.gui.component.NexusUiState;
import nexus.zigliix.com.client.module.ModuleManager;
import nexus.zigliix.com.client.module.modules.hud.ArmorStatus;
import nexus.zigliix.com.client.module.modules.hud.CPS;
import nexus.zigliix.com.client.module.modules.hud.Clock;
import nexus.zigliix.com.client.module.modules.hud.Coordinates;
import nexus.zigliix.com.client.module.modules.hud.FPSDisplay;
import nexus.zigliix.com.client.module.modules.hud.Keystrokes;
import nexus.zigliix.com.client.module.modules.hud.PotionEffects;
import nexus.zigliix.com.client.module.modules.cosmetic.ClientBadge;
import nexus.zigliix.com.client.module.modules.cosmetic.CustomCrosshair;
import nexus.zigliix.com.client.notify.NotificationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HudRenderer {
    private static final EquipmentSlot[] ARMOR_ORDER = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter CLOCK_FORMAT_12H = DateTimeFormatter.ofPattern("hh:mm:ss a");

    private HudRenderer() {}

    public static void renderAll(GuiGraphicsExtractor g, Minecraft client, int screenWidth, int screenHeight) {
        if (client.player == null || client.options.hideGui) {
            return;
        }
        Font font = client.font;
        for (HudBounds bounds : getActiveBounds(screenWidth, screenHeight)) {
            switch (bounds.id()) {
                case "coordinates" -> drawPill(g, font, bounds.x(), bounds.y(), bounds.width(), bounds.text(), NexusTheme.text());
                case "fps" -> drawPill(g, font, bounds.x(), bounds.y(), bounds.width(), bounds.text(), getFpsColor(client.getFps()));
                case "cps" -> drawPill(g, font, bounds.x(), bounds.y(), bounds.width(), bounds.text(), NexusTheme.text());
                case "clock" -> drawPill(g, font, bounds.x(), bounds.y(), bounds.width(), bounds.text(), NexusTheme.accentSoft());
                case "effects" -> renderPotionEffects(g, client, bounds.x(), bounds.y(), bounds.width());
                case "armor" -> renderArmor(g, client, bounds.x(), bounds.y());
                case "keystrokes" -> renderKeystrokes(g, client, bounds.x(), bounds.y());
                default -> { }
            }
        }
        renderCosmetics(g, client, screenWidth, screenHeight);
        NotificationManager.render(g, client, screenWidth, screenHeight);
    }

    private static void renderCosmetics(GuiGraphicsExtractor g, Minecraft client, int screenWidth, int screenHeight) {
        if (ModuleManager.isModuleEnabled(ClientBadge.class)) {
            ClientBadge badge = ModuleManager.getModule(ClientBadge.class);
            renderClientBadge(g, client, screenWidth, screenHeight, badge);
        }

        if (client.screen == null && ModuleManager.isModuleEnabled(CustomCrosshair.class)) {
            CustomCrosshair crosshair = ModuleManager.getModule(CustomCrosshair.class);
            renderCustomCrosshair(g, screenWidth / 2, screenHeight / 2, crosshair);
        }
    }

    private static void renderClientBadge(GuiGraphicsExtractor g, Minecraft client, int screenWidth, int screenHeight, ClientBadge badge) {
        if (badge == null) {
            return;
        }

        Font font = client.font;
        String label = "Nexus";
        String user = client.getUser() != null ? client.getUser().getName() : "Player";
        int width = Math.max(82, font.width(label) + font.width(user) + 30);
        int height = 18;
        int x = switch (badge.getPosition()) {
            case "Top Right", "Bottom Right" -> screenWidth - width - 8;
            default -> 8;
        };
        int y = switch (badge.getPosition()) {
            case "Bottom Left", "Bottom Right" -> screenHeight - height - 8;
            default -> 8;
        };
        y = avoidBadgeOverlap(x, y, width, height, screenWidth, screenHeight, badge.getPosition());

        NexusRenderer.drawDropShadow(g, x, y, width, height, 4);
        NexusRenderer.fillRoundRect(g, x, y, width, height, 6, NexusTheme.panelElevated());
        NexusRenderer.drawOutline(g, x, y, width, height, NexusTheme.withAlpha(NexusTheme.accent(), 130));
        g.fill(x + 5, y + 5, x + 8, y + 13, NexusTheme.accent());
        g.text(font, label, x + 13, y + 5, NexusTheme.text(), true);
        g.text(font, trim(font, user, width - font.width(label) - 24), x + 18 + font.width(label), y + 5, NexusTheme.accentSoft(), false);
    }

    private static int avoidBadgeOverlap(int x, int y, int width, int height, int screenWidth, int screenHeight, String position) {
        boolean bottomAnchored = position != null && position.startsWith("Bottom");
        int adjustedY = y;
        List<HudBounds> activeBounds = getActiveBounds(screenWidth, screenHeight);
        for (int pass = 0; pass < activeBounds.size() + 1; pass++) {
            boolean moved = false;
            for (HudBounds bounds : activeBounds) {
                if (!overlapsWithMargin(x, adjustedY, width, height, bounds, 5)) {
                    continue;
                }
                adjustedY = bottomAnchored ? bounds.y() - height - 6 : bounds.y() + bounds.height() + 6;
                moved = true;
            }
            if (!moved) {
                break;
            }
        }
        return clamp(adjustedY, 2, Math.max(2, screenHeight - height - 2));
    }

    private static boolean overlapsWithMargin(int x, int y, int width, int height, HudBounds bounds, int margin) {
        return x - margin < bounds.x() + bounds.width()
            && x + width + margin > bounds.x()
            && y - margin < bounds.y() + bounds.height()
            && y + height + margin > bounds.y();
    }

    private static void renderCustomCrosshair(GuiGraphicsExtractor g, int cx, int cy, CustomCrosshair crosshair) {
        if (crosshair == null) {
            return;
        }

        int size = crosshair.getSize();
        int gap = crosshair.getGap();
        int thickness = crosshair.getThickness();
        int color = crosshairColor(crosshair);
        int shadow = 0xAA000000;
        boolean outline = crosshair.hasOutline();

        switch (crosshair.getStyle()) {
            case "Dot" -> {
                if (outline) {
                    g.fill(cx - thickness - 1, cy - thickness - 1, cx + thickness + 1, cy + thickness + 1, shadow);
                }
                g.fill(cx - thickness, cy - thickness, cx + thickness, cy + thickness, color);
            }
            case "Box" -> {
                int radius = Math.max(4, size);
                if (outline) {
                    NexusRenderer.drawOutline(g, cx - radius - 1, cy - radius - 1, radius * 2 + 2, radius * 2 + 2, shadow);
                }
                NexusRenderer.drawOutline(g, cx - radius, cy - radius, radius * 2, radius * 2, color);
            }
            case "X" -> {
                drawCrosshairRect(g, cx - gap - size, cy - gap - size, cx - gap, cy - gap, thickness, outline, shadow, color);
                drawCrosshairRect(g, cx + gap, cy - gap - size, cx + gap + size, cy - gap, thickness, outline, shadow, color);
                drawCrosshairRect(g, cx - gap - size, cy + gap, cx - gap, cy + gap + size, thickness, outline, shadow, color);
                drawCrosshairRect(g, cx + gap, cy + gap, cx + gap + size, cy + gap + size, thickness, outline, shadow, color);
            }
            default -> {
                drawCrosshairArm(g, cx - gap - size, cy, cx - gap, cy, thickness, outline, shadow, color);
                drawCrosshairArm(g, cx + gap, cy, cx + gap + size, cy, thickness, outline, shadow, color);
                drawCrosshairArm(g, cx, cy - gap - size, cx, cy - gap, thickness, outline, shadow, color);
                drawCrosshairArm(g, cx, cy + gap, cx, cy + gap + size, thickness, outline, shadow, color);
            }
        }
    }

    private static int crosshairColor(CustomCrosshair crosshair) {
        return switch (crosshair.getColorMode()) {
            case "White" -> 0xFFFFFFFF;
            case "Red" -> NexusTheme.danger();
            case "Mint" -> NexusTheme.success();
            default -> NexusTheme.accent();
        };
    }

    private static void drawCrosshairArm(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int thickness, boolean outline, int shadow, int color) {
        boolean horizontal = y1 == y2;
        int half = Math.max(0, thickness / 2);
        if (horizontal) {
            if (outline) {
                g.fill(Math.min(x1, x2) - 1, y1 - half - 1, Math.max(x1, x2) + 1, y1 + half + 2, shadow);
            }
            g.fill(Math.min(x1, x2), y1 - half, Math.max(x1, x2), y1 + half + 1, color);
            return;
        }

        if (outline) {
            g.fill(x1 - half - 1, Math.min(y1, y2) - 1, x1 + half + 2, Math.max(y1, y2) + 1, shadow);
        }
        g.fill(x1 - half, Math.min(y1, y2), x1 + half + 1, Math.max(y1, y2), color);
    }

    private static void drawCrosshairRect(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int thickness, boolean outline, int shadow, int color) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int steps = Math.max(1, Math.max(maxX - minX, maxY - minY));
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            int x = Math.round(minX + (maxX - minX) * t);
            int y = Math.round(minY + (maxY - minY) * t);
            if (outline) {
                g.fill(x - thickness, y - thickness, x + thickness + 1, y + thickness + 1, shadow);
            }
            g.fill(x, y, x + thickness, y + thickness, color);
        }
    }

    public static void renderEditor(GuiGraphicsExtractor g, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        for (HudBounds bounds : getActiveBounds(screenWidth, screenHeight)) {
            boolean hovered = mouseX >= bounds.x() && mouseX <= bounds.x() + bounds.width() && mouseY >= bounds.y() && mouseY <= bounds.y() + bounds.height();
            NexusRenderer.drawDropShadow(g, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 5);
            NexusRenderer.fillRoundRect(g, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 8, NexusTheme.withAlpha(hovered ? NexusTheme.panelSoft() : NexusTheme.panelFloat(), hovered ? 228 : 214));
            NexusRenderer.drawOutline(g, bounds.x(), bounds.y(), bounds.width(), bounds.height(), NexusTheme.withAlpha(hovered ? NexusTheme.accent() : NexusTheme.separator(), hovered ? 160 : 120));
            g.text(client.font, bounds.id().toUpperCase(), bounds.x() + 6, bounds.y() + 5, hovered ? NexusTheme.text() : NexusTheme.textMuted(), false);
        }
    }

    public static List<HudBounds> getActiveBounds(int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        List<HudBounds> result = new ArrayList<>();
        if (client.player == null) {
            return result;
        }
        Font font = client.font;

        if (ModuleManager.isModuleEnabled(Coordinates.class)) {
            Coordinates coordinates = ModuleManager.getModule(Coordinates.class);
            String text = coordinates != null && !coordinates.shouldShowY()
                ? "XYZ: " + (int) client.player.getX() + " " + (int) client.player.getZ()
                : "XYZ: " + (int) client.player.getX() + " " + (int) client.player.getY() + " " + (int) client.player.getZ();
            result.add(pill("coordinates", 8, 8, text, font));
        }
        if (ModuleManager.isModuleEnabled(FPSDisplay.class)) {
            result.add(pill("fps", 8, 32, "FPS: " + client.getFps(), font));
        }
        if (ModuleManager.isModuleEnabled(CPS.class)) {
            result.add(pill("cps", 8, 56, "CPS: " + CPS.getCPS(), font));
        }
        if (ModuleManager.isModuleEnabled(Clock.class)) {
            Clock clock = ModuleManager.getModule(Clock.class);
            DateTimeFormatter formatter = clock != null && clock.usesTwelveHourClock() ? CLOCK_FORMAT_12H : CLOCK_FORMAT;
            result.add(pill("clock", 8, 80, "Time: " + LocalDateTime.now().format(formatter), font));
        }
        if (ModuleManager.isModuleEnabled(PotionEffects.class)) {
            List<String> effects = potionEffectLines(client);
            int maxWidth = 0;
            for (String text : effects) {
                maxWidth = Math.max(maxWidth, font.width(text) + 16);
            }
            int width = Math.max(72, maxWidth);
            int height = effects.isEmpty() ? 0 : effects.size() * 24 - 6;
            if (!effects.isEmpty()) {
                result.add(box("effects", screenWidth - width - 8, 8, width, height, "effects"));
            }
        }
        if (ModuleManager.isModuleEnabled(ArmorStatus.class)) {
            result.add(box("armor", screenWidth - 98, screenHeight - 84, 90, 78, "armor"));
        }
        if (ModuleManager.isModuleEnabled(Keystrokes.class)) {
            result.add(box("keystrokes", 8, screenHeight - 72, 56, 56, "keystrokes"));
        }
        return applySavedPositions(result);
    }

    private static List<String> potionEffectLines(Minecraft client) {
        List<String> lines = new ArrayList<>();
        if (client.player == null) {
            return lines;
        }

        for (MobEffectInstance effect : client.player.getActiveEffects()) {
            lines.add(effect.getEffect().value().getDisplayName().getString() + " " + formatDuration(effect.getDuration()));
        }
        return lines;
    }

    private static void renderPotionEffects(GuiGraphicsExtractor g, Minecraft client, int x, int y, int width) {
        Font font = client.font;
        int rowY = y;
        for (String text : potionEffectLines(client)) {
            drawPill(g, font, x, rowY, width, text, NexusTheme.text());
            rowY += 24;
        }
    }

    private static List<HudBounds> applySavedPositions(List<HudBounds> bounds) {
        List<HudBounds> result = new ArrayList<>(bounds.size());
        Map<String, NexusUiState.Point> defaultAnchors = new HashMap<>();
        Map<String, NexusUiState.Point> savedAnchors = new HashMap<>();
        for (HudBounds bound : bounds) {
            NexusUiState.Point defaultAnchor = defaultAnchors.computeIfAbsent(bound.id(), id -> new NexusUiState.Point(bound.x(), bound.y()));
            NexusUiState.Point savedAnchor = savedAnchors.computeIfAbsent(bound.id(), id -> NexusUiState.getHudPosition(id, defaultAnchor.x(), defaultAnchor.y()));
            int offsetX = savedAnchor.x() - defaultAnchor.x();
            int offsetY = savedAnchor.y() - defaultAnchor.y();
            Minecraft client = Minecraft.getInstance();
            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();
            int x = clamp(bound.x() + offsetX, 2, Math.max(2, screenWidth - bound.width() - 2));
            int y = clamp(bound.y() + offsetY, 2, Math.max(2, screenHeight - bound.height() - 2));
            result.add(new HudBounds(bound.id(), x, y, bound.width(), bound.height(), bound.text()));
        }
        return result;
    }

    private static HudBounds pill(String id, int x, int y, String text, Font font) {
        return positionedPill(id, x, y, font.width(text) + 16, text);
    }

    private static HudBounds positionedPill(String id, int x, int y, int width, String text) {
        return new HudBounds(id, x, y, width, 18, text);
    }

    private static HudBounds box(String id, int x, int y, int width, int height, String text) {
        return new HudBounds(id, x, y, width, height, text);
    }

    private static void drawPill(GuiGraphicsExtractor g, Font font, int x, int y, int width, String text, int valueColor) {
        NexusRenderer.drawDropShadow(g, x, y, width, 18, 4);
        NexusRenderer.fillRoundRect(g, x, y, width, 18, 8, NexusTheme.panelElevated());
        NexusRenderer.drawOutline(g, x, y, width, 18, NexusTheme.withAlpha(NexusTheme.separator(), 120));
        g.fill(x + 1, y + 1, x + width - 1, y + 3, NexusTheme.withAlpha(NexusTheme.accent(), 90));
        g.text(font, trim(font, text, width - 16), x + 8, y + 5, valueColor, false);
    }

    private static void renderArmor(GuiGraphicsExtractor g, Minecraft client, int x, int y) {
        int rowY = y;
        for (int i = ARMOR_ORDER.length - 1; i >= 0; i--) {
            ItemStack stack = client.player.getItemBySlot(ARMOR_ORDER[i]);
            if (stack.isEmpty() || stack.getMaxDamage() <= 0) {
                continue;
            }
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            int percent = Math.round(remaining * 100.0f / stack.getMaxDamage());
            int color = percent >= 60 ? NexusTheme.success() : percent >= 30 ? NexusTheme.warning() : NexusTheme.danger();
            NexusRenderer.fillRoundRect(g, x, rowY, 90, 18, 8, NexusTheme.panelElevated());
            g.item(stack, x + 2, rowY + 1);
            g.itemDecorations(client.font, stack, x + 2, rowY + 1);
            g.text(client.font, percent + "%", x + 24, rowY + 5, color, false);
            rowY += 20;
        }
    }

    private static void renderKeystrokes(GuiGraphicsExtractor g, Minecraft client, int x, int y) {
        drawKey(g, client, x + 19, y, 18, 18, "W", client.options.keyUp.isDown());
        drawKey(g, client, x, y + 19, 18, 18, "A", client.options.keyLeft.isDown());
        drawKey(g, client, x + 19, y + 19, 18, 18, "S", client.options.keyDown.isDown());
        drawKey(g, client, x + 38, y + 19, 18, 18, "D", client.options.keyRight.isDown());
        drawKey(g, client, x, y + 38, 27, 18, "LMB", client.options.keyAttack.isDown());
        drawKey(g, client, x + 29, y + 38, 27, 18, "RMB", client.options.keyUse.isDown());
    }

    private static void drawKey(GuiGraphicsExtractor g, Minecraft client, int x, int y, int width, int height, String label, boolean pressed) {
        NexusRenderer.fillRoundRect(g, x, y, width, height, 6, pressed ? NexusTheme.accent() : NexusTheme.panelElevated());
        NexusRenderer.drawOutline(g, x, y, width, height, NexusTheme.withAlpha(pressed ? NexusTheme.accentSoft() : NexusTheme.separator(), 120));
        int color = pressed ? NexusTheme.background() : NexusTheme.textMuted();
        g.text(client.font, label, x + (width - client.font.width(label)) / 2, y + 5, color, false);
    }

    private static int getFpsColor(int fps) {
        return fps >= 144 ? NexusTheme.success() : fps >= 60 ? NexusTheme.warning() : NexusTheme.danger();
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String trim(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String value = text;
        while (value.length() > 3 && font.width(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    public record HudBounds(String id, int x, int y, int width, int height, String text) {}
}
