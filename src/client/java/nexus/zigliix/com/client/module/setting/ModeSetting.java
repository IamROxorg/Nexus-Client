package nexus.zigliix.com.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.List;

public final class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String description, String defaultValue, String... modes) {
        super(name, description, defaultValue);
        if (modes.length == 0) {
            throw new IllegalArgumentException("ModeSetting requires at least one mode");
        }
        this.modes = Arrays.asList(modes);
        setStoredValue(resolveMode(defaultValue));
    }

    public List<String> getModes() {
        return modes;
    }

    public void setValue(String value) {
        String resolved = resolveMode(value);
        if (resolved.equals(getValue())) {
            return;
        }

        setStoredValue(resolved);
        valueChanged();
    }

    public void cycle() {
        int index = modes.indexOf(getValue());
        setValue(modes.get((index + 1) % modes.size()));
    }

    private String resolveMode(String value) {
        for (String mode : modes) {
            if (mode.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return modes.getFirst();
    }

    @Override
    public String getType() {
        return "mode";
    }

    @Override
    public String getDisplayValue() {
        return getValue();
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            setStoredValue(resolveMode(element.getAsString()));
        }
    }

    @Override
    public boolean applyCommandValue(String rawValue) {
        String resolved = null;
        for (String mode : modes) {
            if (mode.equalsIgnoreCase(rawValue.trim())) {
                resolved = mode;
                break;
            }
        }

        if (resolved == null) {
            return false;
        }

        setValue(resolved);
        return true;
    }
}
