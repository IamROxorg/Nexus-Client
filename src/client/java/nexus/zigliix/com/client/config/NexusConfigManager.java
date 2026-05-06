package nexus.zigliix.com.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import nexus.zigliix.com.NexusClient;
import nexus.zigliix.com.client.gui.component.NexusUiState;
import nexus.zigliix.com.client.module.Module;
import nexus.zigliix.com.client.module.ModuleManager;
import nexus.zigliix.com.client.module.setting.Setting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class NexusConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("nexus-client.json");

    private static boolean loaded;
    private static boolean suppressSave;
    private static boolean toggleSoundsEnabled = true;
    private static boolean chatFeedbackEnabled;

    private NexusConfigManager() {}

    public static boolean isToggleSoundsEnabled() {
        return toggleSoundsEnabled;
    }

    public static Path getConfigDirectory() {
        return CONFIG_PATH.getParent();
    }

    public static void setToggleSoundsEnabled(boolean enabled) {
        toggleSoundsEnabled = enabled;
        save();
    }

    public static boolean isChatFeedbackEnabled() {
        return chatFeedbackEnabled;
    }

    public static void setChatFeedbackEnabled(boolean enabled) {
        chatFeedbackEnabled = enabled;
        save();
    }

    public static void load() {
        suppressSave = true;
        boolean shouldSaveDefaults = false;

        try {
            resetToDefaults();
            Files.createDirectories(CONFIG_PATH.getParent());
            if (Files.notExists(CONFIG_PATH)) {
                shouldSaveDefaults = true;
            } else {
                String json = Files.readString(CONFIG_PATH);
                if (json.isBlank()) {
                    shouldSaveDefaults = true;
                } else {
                    JsonObject root = parseRoot(json);
                    if (root == null) {
                        shouldSaveDefaults = preserveInvalidConfig();
                    } else {
                        loadGlobals(readObject(root, "global"));
                        NexusUiState.load(readObject(root, "ui"));
                        loadModules(readObject(root, "modules"));
                    }
                }
            }
        } catch (IOException exception) {
            NexusClient.LOGGER.warn("Failed to read Nexus config from {}", CONFIG_PATH, exception);
        } finally {
            suppressSave = false;
            loaded = true;
        }

        if (shouldSaveDefaults) {
            save();
        }
    }

    private static void loadGlobals(JsonObject globals) {
        if (globals == null) {
            return;
        }

        if (globals.has("toggleSounds")) {
            try {
                toggleSoundsEnabled = globals.get("toggleSounds").getAsBoolean();
            } catch (RuntimeException exception) {
                NexusClient.LOGGER.warn("Invalid toggleSounds value in {}", CONFIG_PATH, exception);
            }
        }

        if (globals.has("chatFeedback")) {
            try {
                chatFeedbackEnabled = globals.get("chatFeedback").getAsBoolean();
            } catch (RuntimeException exception) {
                NexusClient.LOGGER.warn("Invalid chatFeedback value in {}", CONFIG_PATH, exception);
            }
        }
    }

    private static void loadModules(JsonObject modulesObject) {
        for (Module module : ModuleManager.getAll()) {
            if (modulesObject == null) {
                continue;
            }

            JsonObject moduleObject = readObject(modulesObject, module.getName());
            if (moduleObject == null) {
                continue;
            }

            if (moduleObject.has("keybind")) {
                try {
                    module.setKeybind(moduleObject.get("keybind").getAsInt(), true);
                } catch (RuntimeException exception) {
                    NexusClient.LOGGER.warn("Invalid keybind for module {}", module.getName(), exception);
                }
            }

            JsonObject settingsObject = readObject(moduleObject, "settings");
            if (settingsObject != null) {
                for (Setting<?> setting : module.getSettings()) {
                    JsonElement element = settingsObject.get(setting.getName());
                    if (element != null) {
                        try {
                            setting.fromJson(element);
                        } catch (RuntimeException exception) {
                            NexusClient.LOGGER.warn("Invalid config for {}.{}", module.getName(), setting.getName(), exception);
                        }
                    }
                }
            }

            if (moduleObject.has("enabled")) {
                try {
                    module.setEnabled(moduleObject.get("enabled").getAsBoolean(), true);
                } catch (RuntimeException exception) {
                    NexusClient.LOGGER.warn("Invalid enabled state for module {}", module.getName(), exception);
                }
            }
        }
    }

    private static void resetToDefaults() {
        toggleSoundsEnabled = true;
        chatFeedbackEnabled = false;
        NexusUiState.resetDefaults();
        for (Module module : ModuleManager.getAll()) {
            module.resetToDefaults(true);
        }
    }

    public static void save() {
        if (!loaded || suppressSave) {
            return;
        }

        JsonObject root = new JsonObject();
        JsonObject globals = new JsonObject();
        globals.addProperty("toggleSounds", toggleSoundsEnabled);
        globals.addProperty("chatFeedback", chatFeedbackEnabled);
        root.add("global", globals);
        root.add("ui", NexusUiState.toJson());

        JsonObject modulesObject = new JsonObject();
        List<Module> modules = ModuleManager.getAll();
        for (Module module : modules) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("enabled", module.isEnabled());
            moduleObject.addProperty("keybind", module.getKeybind());

            JsonObject settingsObject = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                settingsObject.add(setting.getName(), setting.toJson());
            }

            moduleObject.add("settings", settingsObject);
            modulesObject.add(module.getName(), moduleObject);
        }

        root.add("modules", modulesObject);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Path tempPath = CONFIG_PATH.resolveSibling("nexus-client.json.tmp");
            Files.writeString(tempPath, GSON.toJson(root));
            moveConfigIntoPlace(tempPath);
        } catch (IOException exception) {
            NexusClient.LOGGER.warn("Failed to write Nexus config to {}", CONFIG_PATH, exception);
        }
    }

    private static void moveConfigIntoPlace(Path tempPath) throws IOException {
        try {
            Files.move(tempPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(tempPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static JsonObject parseRoot(String json) {
        try {
            JsonElement root = JsonParser.parseString(json);
            if (!root.isJsonObject()) {
                throw new IllegalStateException("Config root is not an object");
            }
            return root.getAsJsonObject();
        } catch (RuntimeException exception) {
            NexusClient.LOGGER.warn("Failed to parse Nexus config at {}", CONFIG_PATH, exception);
            return null;
        }
    }

    private static JsonObject readObject(JsonObject object, String key) {
        if (object == null || !object.has(key)) {
            return null;
        }

        try {
            JsonElement element = object.get(key);
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (RuntimeException exception) {
            NexusClient.LOGGER.warn("Invalid object value for {} in {}", key, CONFIG_PATH, exception);
            return null;
        }
    }

    private static boolean preserveInvalidConfig() {
        Path backupPath = CONFIG_PATH.resolveSibling("nexus-client.invalid-" + System.currentTimeMillis() + ".json");
        try {
            Files.move(CONFIG_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
            NexusClient.LOGGER.warn("Moved invalid Nexus config to {}", backupPath);
            return true;
        } catch (IOException exception) {
            NexusClient.LOGGER.warn("Failed to preserve invalid Nexus config at {}", CONFIG_PATH, exception);
            return false;
        }
    }
}
