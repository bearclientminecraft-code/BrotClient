package dev.brot.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

public class ModeSetting extends Setting<String> {
    public final List<String> modes;
    public ModeSetting(String name, String def, String... modes) {
        super(name, def); this.modes = List.of(modes);
    }
    public boolean is(String m) { return value.equals(m); }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { if (modes.contains(e.getAsString())) set(e.getAsString()); }
}
