package nexus.zigliix.com.client.module;

import nexus.zigliix.com.NexusClient;
import nexus.zigliix.com.client.module.modules.combat.AutoRespawn;
import nexus.zigliix.com.client.module.modules.cosmetic.ClientBadge;
import nexus.zigliix.com.client.module.modules.cosmetic.CustomCrosshair;
import nexus.zigliix.com.client.module.modules.cosmetic.NexusCape;
import nexus.zigliix.com.client.module.modules.hud.ArmorStatus;
import nexus.zigliix.com.client.module.modules.hud.CPS;
import nexus.zigliix.com.client.module.modules.hud.Clock;
import nexus.zigliix.com.client.module.modules.hud.Coordinates;
import nexus.zigliix.com.client.module.modules.hud.FPSDisplay;
import nexus.zigliix.com.client.module.modules.hud.Keystrokes;
import nexus.zigliix.com.client.module.modules.hud.PotionEffects;
import nexus.zigliix.com.client.module.modules.misc.Zoom;
import nexus.zigliix.com.client.module.modules.movement.Sprint;
import nexus.zigliix.com.client.module.modules.visual.FullBright;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import nexus.zigliix.com.client.util.KeybindUtil;

public final class ModuleManager {
    private ModuleManager() {}

    private static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        MODULES.clear();
        register(new AutoRespawn());
        register(new FullBright());
        register(new Sprint());
        register(new Zoom());
        register(new Coordinates());
        register(new FPSDisplay());
        register(new Clock());
        register(new Keystrokes());
        register(new PotionEffects());
        register(new ArmorStatus());
        register(new CPS());
        register(new ClientBadge());
        register(new CustomCrosshair());
        register(new NexusCape());
    }

    private static void register(Module m) {
        String normalizedName = KeybindUtil.normalizeName(m.getName());
        boolean duplicateName = MODULES.stream()
            .map(Module::getName)
            .map(KeybindUtil::normalizeName)
            .anyMatch(normalizedName::equals);
        if (duplicateName) {
            NexusClient.LOGGER.warn("Skipping duplicate module registration for {}", m.getName());
            return;
        }

        m.captureDefaults();
        MODULES.add(m);
    }

    public static List<Module> getAll() { return Collections.unmodifiableList(MODULES); }

    public static List<Module> getByCategory(Category category) {
        return MODULES.stream()
            .filter(m -> m.getCategory() == category)
            .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public static List<Module> getEnabled() {
        return MODULES.stream()
            .filter(Module::isEnabled)
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .toList();
    }

    public static boolean isModuleEnabled(Class<? extends Module> moduleClass) {
        return MODULES.stream()
            .filter(m -> moduleClass.isInstance(m))
            .findFirst()
            .map(Module::isEnabled)
            .orElse(false);
    }

    public static <T extends Module> T getModule(Class<T> moduleClass) {
        return MODULES.stream()
            .filter(moduleClass::isInstance)
            .map(moduleClass::cast)
            .findFirst()
            .orElse(null);
    }

    public static void tickModules() {
        for (Module module : MODULES) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Throwable throwable) {
                    NexusClient.LOGGER.error("Module {} failed during tick; disabling it.", module.getName(), throwable);
                    module.setEnabled(false);
                }
            }
        }
    }

    public static boolean handleKeyPress(int keyCode) {
        boolean handled = false;
        for (Module module : MODULES) {
            if (module.getKeybind() != KeybindUtil.NONE && module.getKeybind() == keyCode) {
                module.toggle();
                handled = true;
            }
        }
        return handled;
    }

    public static Module findByName(String query) {
        String normalized = KeybindUtil.normalizeName(query);
        return MODULES.stream()
            .filter(module -> KeybindUtil.normalizeName(module.getName()).equals(normalized))
            .findFirst()
            .orElse(null);
    }
}
