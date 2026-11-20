package dev.brot.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {
    public final double min, max, step;
    public String unit = "";
    public NumberSetting unit(String u) { unit = u; return this; }
    public NumberSetting(String name, double def, double min, double max, double step) {
        super(name, def); this.min = min; this.max = max; this.step = step;
    }
    public float f() { return value.floatValue(); }
    public int i() { return (int) Math.round(value); }
    public void setClamped(double v) {
        if (step > 0) v = Math.round(v / step) * step;
        set(Math.max(min, Math.min(max, v)));
    }
    public double ratio() { return (value - min) / (max - min); }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { setClamped(e.getAsDouble()); }
}
