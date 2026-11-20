package dev.brot.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting<Boolean> {
    /** Rendered as a checkbox instead of a switch. */
    public boolean checkbox;
    public BooleanSetting(String name, boolean def) { super(name, def); }
    public BooleanSetting asCheckbox() { checkbox = true; return this; }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { value = e.getAsBoolean(); }
}
