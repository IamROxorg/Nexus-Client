package nexus.zigliix.com.client.module;

import nexus.zigliix.com.NexusClient;
import nexus.zigliix.com.client.config.NexusConfigManager;
import nexus.zigliix.com.client.module.setting.Setting;
import nexus.zigliix.com.client.notify.NotificationManager;
import nexus.zigliix.com.client.util.KeybindUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean defaultEnabled;
    private int defaultKeybind = KeybindUtil.NONE;
    private boolean enabled;
    private int keybind = KeybindUtil.NONE;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <T extends Setting<?>> T addSetting(T setting) {
        setting.attach(this);
        settings.add(setting);
        return setting;
    }

    public void onEnable()  {}
    public void onDisable() {}
    public void onTick()    {}

    public void setEnabled(boolean enabled) {
        setEnabled(enabled, false);
    }

    public void setEnabled(boolean enabled, boolean silent) {
        if (this.enabled == enabled) {
            return;
        }

        boolean previousState = this.enabled;
        this.enabled = enabled;

        try {
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Throwable throwable) {
            this.enabled = previousState;
            NexusClient.LOGGER.error("Module {} failed to {}", name, enabled ? "enable" : "disable", throwable);
            if (!silent) {
                NotificationManager.error(moduleActionFailedMessage(enabled));
            }
            return;
        }

        if (!silent) {
            NotificationManager.moduleToggled(this, enabled);
            NexusConfigManager.save();
        }
    }

    public void setKeybind(int keybind) {
        setKeybind(keybind, false);
    }

    public void setKeybind(int keybind, boolean silent) {
        this.keybind = KeybindUtil.isReserved(keybind) ? KeybindUtil.NONE : keybind;
        if (!silent) {
            NexusConfigManager.save();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void captureDefaults() {
        defaultEnabled = enabled;
        defaultKeybind = keybind;
    }

    public void resetToDefaults(boolean silent) {
        for (Setting<?> setting : settings) {
            setting.resetToDefault();
        }

        setKeybind(defaultKeybind, true);
        setEnabled(defaultEnabled, silent);
    }

    public String   getName()       { return name; }
    public String   getDescription(){ return description; }
    public Category getCategory()   { return category; }
    public boolean  isEnabled()     { return enabled; }
    public int getKeybind()         { return keybind; }
    public boolean isDefaultEnabled() { return defaultEnabled; }
    public int getDefaultKeybind() { return defaultKeybind; }

    public String getKeybindName() {
        return KeybindUtil.getKeyName(keybind);
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public Setting<?> findSetting(String query) {
        String normalized = KeybindUtil.normalizeName(query);
        return settings.stream()
            .filter(setting -> KeybindUtil.normalizeName(setting.getName()).equals(normalized))
            .findFirst()
            .orElse(null);
    }

    private String moduleActionFailedMessage(boolean enabling) {
        return "Failed to " + (enabling ? "enable " : "disable ") + name;
    }
}
