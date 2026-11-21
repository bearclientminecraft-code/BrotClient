package dev.brot.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.brot.module.Module;

/**
 * The "Bind Key" row every module gets. It is a live view of {@link Module#bind()} (GLFW key, or MOUSE_BASE + button,
 * -1 = unbound), so there is exactly one stored value; the config keeps writing it as the module's "bind" field.
 */
public class BindSetting extends Setting<Integer> {
    public final Module module;

    public BindSetting(Module module) {
        super("Bind Key", -1);
        this.module = module;
        this.description = "Key or mouse button that toggles this module. Click to set, right-click or Backspace to clear";
    }

    @Override public Integer get() { return module.bind(); }
    @Override public void set(Integer v) { if (v >= -1 && v != module.bind()) { module.setBind(v); onChange.accept(v); } }
    public boolean isSet() { return module.bind() >= 0; }

    @Override public boolean persistent() { return false; }
    @Override public JsonElement toJson() { return new JsonPrimitive(module.bind()); }
    @Override public void fromJson(JsonElement e) { set(e.getAsInt()); }
}
