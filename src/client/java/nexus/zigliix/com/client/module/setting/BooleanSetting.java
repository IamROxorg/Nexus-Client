package nexus.zigliix.com.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public void setValue(boolean value) {
        if (getValue() == value) {
            return;
        }

        setStoredValue(value);
        valueChanged();
    }

    public void toggle() {
        setValue(!getValue());
    }

    @Override
    public String getType() {
        return "boolean";
    }

    @Override
    public String getDisplayValue() {
        return getValue() ? "true" : "false";
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            setStoredValue(element.getAsBoolean());
        }
    }

    @Override
    public boolean applyCommandValue(String rawValue) {
        String normalized = rawValue.trim().toLowerCase();
        return switch (normalized) {
            case "true", "on", "yes", "1" -> {
                setValue(true);
                yield true;
            }
            case "false", "off", "no", "0" -> {
                setValue(false);
                yield true;
            }
            case "toggle" -> {
                toggle();
                yield true;
            }
            default -> false;
        };
    }
}
