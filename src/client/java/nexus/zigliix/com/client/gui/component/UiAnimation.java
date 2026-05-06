package nexus.zigliix.com.client.gui.component;

public final class UiAnimation {
    private UiAnimation() {}

    public static float approach(float value, float target, float speed) {
        if (value == target) {
            return value;
        }
        if (value < target) {
            return Math.min(target, value + speed);
        }
        return Math.max(target, value - speed);
    }

    public static float toggle(float value, boolean state, float speed) {
        return approach(value, state ? 1.0f : 0.0f, speed);
    }

    public static float easeOutCubic(float value) {
        return 1.0f - (float) Math.pow(1.0f - clamp01(value), 3.0);
    }

    public static float easeOutQuad(float value) {
        float t = clamp01(value);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    public static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
