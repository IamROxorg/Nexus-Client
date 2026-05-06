package nexus.zigliix.com.client.notify;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import nexus.zigliix.com.client.config.NexusConfigManager;
import nexus.zigliix.com.client.gui.component.NexusRenderer;
import nexus.zigliix.com.client.gui.component.NexusTheme;
import nexus.zigliix.com.client.gui.component.UiAnimation;
import nexus.zigliix.com.client.module.Module;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NotificationManager {
    private static final long DURATION_MS = 2600L;
    private static final long FADE_MS = 220L;
    private static final List<Notification> NOTIFICATIONS = new ArrayList<>();

    private NotificationManager() {}

    private record Notification(String message, int color, long startTime) {}

    public static void tick() {
        long now = System.currentTimeMillis();
        Iterator<Notification> iterator = NOTIFICATIONS.iterator();
        while (iterator.hasNext()) {
            Notification notification = iterator.next();
            if (now - notification.startTime() > DURATION_MS + FADE_MS) {
                iterator.remove();
            }
        }
    }

    public static void push(String message, int color) {
        if (NOTIFICATIONS.size() >= 5) {
            NOTIFICATIONS.remove(0);
        }
        NOTIFICATIONS.add(new Notification(message, color, System.currentTimeMillis()));
    }

    public static void moduleToggled(Module module, boolean enabled) {
        push((enabled ? "Enabled " : "Disabled ") + module.getName(), enabled ? NexusTheme.success() : NexusTheme.danger());
        if (NexusConfigManager.isChatFeedbackEnabled()) {
            chatFeedback(module.getName() + " -> " + (enabled ? "enabled" : "disabled"));
        }
        playToggleSound(enabled);
    }

    public static void info(String message) {
        info(message, false);
    }

    public static void info(String message, boolean forceChat) {
        push(message, NexusTheme.accentSoft());
        if (forceChat || NexusConfigManager.isChatFeedbackEnabled()) {
            chatFeedback(message);
        }
    }

    public static void error(String message) {
        push(message, NexusTheme.danger());
        chatFeedback(message);
    }

    public static void chat(String message) {
        chatFeedback(message);
    }

    private static void chatFeedback(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("[Nexus] " + message));
        }
    }

    private static void playToggleSound(boolean enabled) {
        if (!NexusConfigManager.isToggleSoundsEnabled()) {
            return;
        }

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, enabled ? 1.12F : 0.88F));
    }

    public static void render(GuiGraphicsExtractor g, Minecraft client, int screenWidth, int screenHeight) {
        Font font = client.font;
        int y = screenHeight - 30;
        long now = System.currentTimeMillis();

        for (int i = NOTIFICATIONS.size() - 1; i >= 0; i--) {
            Notification notification = NOTIFICATIONS.get(i);
            long age = now - notification.startTime();
            float alphaProgress = Math.min(1.0F, age / (float) FADE_MS);
            if (age > DURATION_MS) {
                alphaProgress = Math.max(0.0F, 1.0F - (age - DURATION_MS) / (float) FADE_MS);
            }
            float eased = UiAnimation.easeOutCubic(alphaProgress);
            int maxTextWidth = Math.max(80, Math.min(260, screenWidth - 42));
            String message = trim(font, notification.message(), maxTextWidth);
            int width = font.width(message) + 22;
            int x = screenWidth - width - 8 + (int) ((1.0F - eased) * 18.0F);
            int bg = NexusTheme.withAlpha(NexusTheme.panelElevated(), (int) (225 * eased));
            int accent = NexusTheme.withAlpha(notification.color(), (int) (255 * eased));
            int textColor = NexusTheme.withAlpha(NexusTheme.text(), (int) (255 * eased));

            NexusRenderer.fillRoundRect(g, x, y, width, 18, 6, bg);
            g.fill(x, y + 4, x + 2, y + 14, accent);
            g.text(font, message, x + 8, y + 5, textColor, false);
            y -= 21;
        }
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
}
