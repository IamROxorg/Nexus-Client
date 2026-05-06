package nexus.zigliix.com.client.gui.component;

public final class NexusTheme {
    public static final int HUD_PADDING = 6;
    public static final int WINDOW_RADIUS = 14;
    public static final int PANEL_RADIUS = 12;
    public static final int ELEMENT_RADIUS = 9;
    public static final int PANEL_GAP = 12;
    public static final int PANEL_WIDTH = 220;
    public static final int PANEL_HEADER_H = 32;

    private NexusTheme() {}

    public static int background() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0xFF05070A;
            case MIDNIGHT -> 0xFF060A0E;
            default -> 0xFF060A0D;
        };
    }

    public static int backgroundAlt() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0xD70B1117;
            case MIDNIGHT -> 0xD70D151D;
            default -> 0xD70D141A;
        };
    }

    public static int panel() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0x66141C24;
            case MIDNIGHT -> 0x6618232D;
            default -> 0x6619232D;
        };
    }

    public static int panelAlt() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0x7A1B2530;
            case MIDNIGHT -> 0x7A202D38;
            default -> 0x7A202A34;
        };
    }

    public static int panelElevated() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0x8425303A;
            case MIDNIGHT -> 0x842B3844;
            default -> 0x842A3540;
        };
    }

    public static int panelSoft() {
        return NexusRenderer.lerpColor(panelAlt(), withAlpha(accent(), 255), 0.08f);
    }

    public static int panelHeader() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0x6F18212A;
            case MIDNIGHT -> 0x6F1C2934;
            default -> 0x6F1C2630;
        };
    }

    public static int panelFloat() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0x58141C24;
            case MIDNIGHT -> 0x5818232D;
            default -> 0x5819232D;
        };
    }

    public static int overlay() {
        return 0x56070B11;
    }

    public static int overlaySoft() {
        return 0x32070B11;
    }

    public static int accent() {
        return NexusUiState.getAccentColor();
    }

    public static int accentSoft() {
        return NexusRenderer.lerpColor(accent(), text(), 0.70f);
    }

    public static int accentDark() {
        return NexusRenderer.lerpColor(accent(), background(), 0.45f);
    }

    public static int success() {
        return 0xFF8EE0B1;
    }

    public static int danger() {
        return 0xFFFF9B97;
    }

    public static int warning() {
        return 0xFFF5CA73;
    }

    public static int text() {
        return 0xFFF2F6FB;
    }

    public static int textMuted() {
        return 0xFFD4E0EA;
    }

    public static int textDim() {
        return 0xFF9FAFBD;
    }

    public static int separator() {
        return 0x42FFFFFF;
    }

    public static int shadow() {
        return 0xCC04070C;
    }

    public static int withAlpha(int color, int alpha) {
        return NexusRenderer.withAlpha(color, alpha);
    }
}
