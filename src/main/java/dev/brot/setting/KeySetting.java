package dev.brot.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.lwjgl.glfw.GLFW;

/**
 * GLFW keyboard key, -1 = unbound. A "hotbar" key only accepts the number keys 1-9 and is read as a hotbar slot
 * (slot() = 0..8); it can never be cleared, so a module always has a valid slot.
 */
public class KeySetting extends Setting<Integer> {
    public final boolean hotbar;

    private KeySetting(String name, int def, boolean hotbar) { super(name, def); this.hotbar = hotbar; }
    public static KeySetting key(String name) { return new KeySetting(name, -1, false); }
    /** @param slot 1..9 */
    public static KeySetting hotbar(String name, int slot) { return new KeySetting(name, GLFW.GLFW_KEY_1 + slot - 1, true); }

    public boolean valid(int k) {
        return hotbar ? k >= GLFW.GLFW_KEY_1 && k <= GLFW.GLFW_KEY_9 : k >= -1;
    }
    public boolean isSet() { return value >= 0; }
    /** Hotbar index 0..8, or -1 when not a hotbar key. */
    public int slot() { return hotbar && valid(value) ? value - GLFW.GLFW_KEY_1 : -1; }

    @Override public void set(Integer v) { if (valid(v) && !v.equals(value)) super.set(v); }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { int k = e.getAsInt(); if (valid(k)) value = k; }
}
