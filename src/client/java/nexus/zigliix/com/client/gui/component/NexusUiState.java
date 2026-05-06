package nexus.zigliix.com.client.gui.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nexus.zigliix.com.client.config.NexusConfigManager;
import nexus.zigliix.com.client.module.Category;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class NexusUiState {
    private static final EnumMap<Category, Point> PANEL_POSITIONS = new EnumMap<>(Category.class);
    private static final Map<String, Point> HUD_POSITIONS = new HashMap<>();
    private static ThemeVariant themeVariant = ThemeVariant.GLACIAL;
    private static AccentPreset accentPreset = AccentPreset.FROST;

    private NexusUiState() {}

    public static void resetDefaults() {
        themeVariant = ThemeVariant.GLACIAL;
        accentPreset = AccentPreset.FROST;
        PANEL_POSITIONS.clear();
        HUD_POSITIONS.clear();
    }

    public static ThemeVariant getThemeVariant() {
        return themeVariant;
    }

    public static void setThemeVariant(ThemeVariant variant) {
        if (variant == null || themeVariant == variant) {
            return;
        }
        themeVariant = variant;
        NexusConfigManager.save();
    }

    public static AccentPreset getAccentPreset() {
        return accentPreset;
    }

    public static int getAccentColor() {
        return accentPreset.color;
    }

    public static void setAccentPreset(AccentPreset preset) {
        if (preset == null || accentPreset == preset) {
            return;
        }
        accentPreset = preset;
        NexusConfigManager.save();
    }

    public static Point getPanelPosition(Category category, int fallbackX, int fallbackY) {
        return PANEL_POSITIONS.getOrDefault(category, new Point(fallbackX, fallbackY));
    }

    public static void setPanelPosition(Category category, int x, int y) {
        Point current = PANEL_POSITIONS.get(category);
        if (current != null && current.x == x && current.y == y) {
            return;
        }
        PANEL_POSITIONS.put(category, new Point(x, y));
        NexusConfigManager.save();
    }

    public static Point getHudPosition(String id, int fallbackX, int fallbackY) {
        return HUD_POSITIONS.getOrDefault(id, new Point(fallbackX, fallbackY));
    }

    public static void setHudPosition(String id, int x, int y) {
        setHudPosition(id, x, y, true);
    }

    public static void setHudPosition(String id, int x, int y, boolean save) {
        Point current = HUD_POSITIONS.get(id);
        if (current != null && current.x == x && current.y == y) {
            return;
        }
        HUD_POSITIONS.put(id, new Point(x, y));
        if (save) {
            NexusConfigManager.save();
        }
    }

    public static void resetHudPositions() {
        if (HUD_POSITIONS.isEmpty()) {
            return;
        }
        HUD_POSITIONS.clear();
        NexusConfigManager.save();
    }

    public static void resetUiPreferences() {
        themeVariant = ThemeVariant.GLACIAL;
        accentPreset = AccentPreset.FROST;
        PANEL_POSITIONS.clear();
        HUD_POSITIONS.clear();
        NexusConfigManager.save();
    }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("theme", themeVariant.id);
        root.addProperty("accent", accentPreset.id);

        JsonObject panels = new JsonObject();
        for (Map.Entry<Category, Point> entry : PANEL_POSITIONS.entrySet()) {
            JsonObject point = new JsonObject();
            point.addProperty("x", entry.getValue().x);
            point.addProperty("y", entry.getValue().y);
            panels.add(entry.getKey().name(), point);
        }
        root.add("panels", panels);

        JsonObject hud = new JsonObject();
        for (Map.Entry<String, Point> entry : HUD_POSITIONS.entrySet()) {
            JsonObject point = new JsonObject();
            point.addProperty("x", entry.getValue().x);
            point.addProperty("y", entry.getValue().y);
            hud.add(entry.getKey(), point);
        }
        root.add("hud", hud);
        return root;
    }

    public static void load(JsonObject object) {
        if (object == null) {
            return;
        }

        themeVariant = ThemeVariant.fromId(readString(object, "theme"), ThemeVariant.GLACIAL);
        accentPreset = AccentPreset.fromId(readString(object, "accent"), AccentPreset.FROST);

        PANEL_POSITIONS.clear();
        JsonObject panels = readObject(object, "panels");
        if (panels != null) {
            for (Category category : Category.values()) {
                JsonObject point = readObject(panels, category.name());
                if (point != null) {
                    PANEL_POSITIONS.put(category, new Point(readInt(point, "x", 0), readInt(point, "y", 0)));
                }
            }
        }

        HUD_POSITIONS.clear();
        JsonObject hud = readObject(object, "hud");
        if (hud != null) {
            for (Map.Entry<String, JsonElement> entry : hud.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject point = entry.getValue().getAsJsonObject();
                    HUD_POSITIONS.put(entry.getKey(), new Point(readInt(point, "x", 0), readInt(point, "y", 0)));
                }
            }
        }
    }

    private static String readString(JsonObject object, String key) {
        try {
            return object.has(key) ? object.get(key).getAsString() : "";
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private static int readInt(JsonObject object, String key, int fallback) {
        try {
            return object.has(key) ? object.get(key).getAsInt() : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public record Point(int x, int y) {}

    public enum ThemeVariant {
        GLACIAL("glacial"),
        OBSIDIAN("obsidian"),
        MIDNIGHT("midnight");

        private final String id;

        ThemeVariant(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public static ThemeVariant fromId(String id, ThemeVariant fallback) {
            if (id == null) {
                return fallback;
            }
            String normalized = id.toLowerCase(Locale.ROOT);
            for (ThemeVariant variant : values()) {
                if (variant.id.equals(normalized)) {
                    return variant;
                }
            }
            return fallback;
        }
    }

    public enum AccentPreset {
        FROST("frost", 0xFFEAFBFF),
        AQUA("aqua", 0xFF8DECF2),
        MINT("mint", 0xFFA9F2D0);

        private final String id;
        private final int color;

        AccentPreset(String id, int color) {
            this.id = id;
            this.color = color;
        }

        public String id() {
            return id;
        }

        public int color() {
            return color;
        }

        public static AccentPreset fromId(String id, AccentPreset fallback) {
            if (id == null) {
                return fallback;
            }
            String normalized = id.toLowerCase(Locale.ROOT);
            if (normalized.equals("soft_violet") || normalized.equals("ice_blue") || normalized.equals("arctic_mint")) {
                return fallback;
            }
            for (AccentPreset preset : values()) {
                if (preset.id.equals(normalized)) {
                    return preset;
                }
            }
            return fallback;
        }
    }

    private static JsonObject readObject(JsonObject object, String key) {
        try {
            if (object == null || !object.has(key) || !object.get(key).isJsonObject()) {
                return null;
            }
            return object.get(key).getAsJsonObject();
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
