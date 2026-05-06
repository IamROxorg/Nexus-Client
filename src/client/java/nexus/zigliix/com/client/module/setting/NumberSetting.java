package nexus.zigliix.com.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        setStoredValue(normalize(defaultValue));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public void setValue(double value) {
        setValue(value, true);
    }

    public void setValue(double value, boolean save) {
        double normalized = normalize(value);
        if (Double.compare(getValue(), normalized) == 0) {
            return;
        }

        setStoredValue(normalized);
        if (save) {
            valueChanged();
        }
    }

    private double normalize(double value) {
        if (!Double.isFinite(value)) {
            return min;
        }

        double clamped = Math.max(min, Math.min(max, value));
        if (step <= 0.0D) {
            return clamped;
        }

        double steps = Math.round((clamped - min) / step);
        double stepped = min + (steps * step);
        return Math.max(min, Math.min(max, stepped));
    }

    @Override
    public String getType() {
        return "number";
    }

    @Override
    public String getDisplayValue() {
        double value = getValue();
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return Integer.toString((int) Math.rint(value));
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            setStoredValue(normalize(element.getAsDouble()));
        }
    }

    @Override
    public boolean applyCommandValue(String rawValue) {
        try {
            double value = Double.parseDouble(rawValue.trim().replace(',', '.'));
            if (!Double.isFinite(value)) {
                return false;
            }

            setValue(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
