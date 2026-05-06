package nexus.zigliix.com.client.module.setting;

import com.google.gson.JsonElement;
import nexus.zigliix.com.client.config.NexusConfigManager;
import nexus.zigliix.com.client.module.Module;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    private final T defaultValue;
    private T value;
    private Module module;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final T getValue() {
        return value;
    }

    public final Module getModule() {
        return module;
    }

    public final T getDefaultValue() {
        return defaultValue;
    }

    public final void attach(Module module) {
        this.module = module;
    }

    protected final void setStoredValue(T value) {
        this.value = value;
    }

    public void resetToDefault() {
        setStoredValue(defaultValue);
    }

    protected final void valueChanged() {
        NexusConfigManager.save();
    }

    public abstract String getType();

    public abstract String getDisplayValue();

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement element);

    public abstract boolean applyCommandValue(String rawValue);
}
