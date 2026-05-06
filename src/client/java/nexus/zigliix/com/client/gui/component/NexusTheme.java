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
            case OBSIDIAN -> 0xFF070809;
            case MIDNIGHT -> 0xFF090B0E;
            default -> 0xFF080A0D;
        };
    }

    public static int backgroundAlt() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0xFF101113;
            case MIDNIGHT -> 0xFF121419;
            default -> 0xFF111418;
        };
    }

    public static int panel() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0xF017191C;
            case MIDNIGHT -> 0xF0191D24;
            default -> 0xF0181B21;
        };
    }

    public static int panelAlt() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0xF01E2125;
            case MIDNIGHT -> 0xF0202530;
            default -> 0xF01F232B;
        };
    }

    public static int panelElevated() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0xF025292E;
            case MIDNIGHT -> 0xF0262D38;
            default -> 0xF0252A33;
        };
    }

    public static int panelSoft() {
        return NexusRenderer.lerpColor(panelAlt(), withAlpha(accent(), 255), 0.12f);
    }

    public static int panelHeader() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0xF01C1F23;
            case MIDNIGHT -> 0xF01D222B;
            default -> 0xF01C2027;
        };
    }

    public static int panelFloat() {
        return switch (NexusUiState.getThemeVariant()) {
            case OBSIDIAN -> 0xE615171A;
            case MIDNIGHT -> 0xE6171A20;
            default -> 0xE6161920;
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
        return NexusRenderer.lerpColor(accent(), text(), 0.58f);
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
        return 0xFFA2AFBF;
    }

    public static int textDim() {
        return 0xFF7F8C9E;
    }

    public static int separator() {
        return 0x66545A64;
    }

    public static int shadow() {
        return 0xCC04070C;
    }

    public static int withAlpha(int color, int alpha) {
        return NexusRenderer.withAlpha(color, alpha);
    }
}
