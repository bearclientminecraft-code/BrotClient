package dev.brot.module;

import dev.brot.setting.KeySetting;
import dev.brot.setting.Setting;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/** Per-tick keyboard state shared by all modules (one GLFW poll per key per tick, edge detection). */
public final class KeybindManager {
    private KeybindManager() {}
    private static Map<Integer, Boolean> cur = new HashMap<>(), prev = new HashMap<>();

    /** Called once at the start of every client tick (ModuleManager.tick). */
    /** Key codes >= MOUSE_BASE are mouse buttons (MOUSE_BASE + GLFW button). */
    public static final int MOUSE_BASE = 1000;
    public static int mouse(int button) { return MOUSE_BASE + button; }

    /**
     * Called once at the start of every client tick. Polls EVERY key setting of every module (not only those a module
     * happened to ask about), so "pressed" edges are never missed when a module skipped its check on the previous tick.
     */
    public static void beginTick() {
        Map<Integer, Boolean> t = prev; prev = cur; cur = t; cur.clear();   // reuse the two maps: no allocation per tick
        for (Module m : ModuleManager.all())
            for (Setting<?> s : m.settings())
                if (s instanceof KeySetting k && !k.hotbar) down(k.get());
    }

    public static boolean down(int key) {
        if (key < 0) return false;
        Boolean c = cur.get(key);
        if (c != null) return c;
        var w = MinecraftClient.getInstance().getWindow();
        if (w == null) return false;
        boolean d = key >= MOUSE_BASE ? GLFW.glfwGetMouseButton(w.getHandle(), key - MOUSE_BASE) == GLFW.GLFW_PRESS
                                      : GLFW.glfwGetKey(w.getHandle(), key) == GLFW.GLFW_PRESS;
        cur.put(key, d);
        return d;
    }
    /** True only on the tick the key/button went down. */
    public static boolean pressed(int key) { return down(key) && !prev.getOrDefault(key, false); }
    public static boolean pressed(KeySetting s) { return pressed(s.get()); }
    public static boolean down(KeySetting s) { return down(s.get()); }

    public static String name(int key, boolean hotbar) {
        if (key < 0) return "None";
        if (hotbar) return String.valueOf(key - GLFW.GLFW_KEY_0);
        if (key >= MOUSE_BASE) return switch (key - MOUSE_BASE) {
            case 0 -> "LMB"; case 1 -> "RMB"; case 2 -> "MMB"; default -> "Mouse " + (key - MOUSE_BASE + 1);
        };
        String n = switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> "Shift";
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> "Ctrl";
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> "Alt";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            default -> null;
        };
        if (n != null) return n;
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        String g = GLFW.glfwGetKeyName(key, 0);
        return g != null ? g.toUpperCase(Locale.ROOT) : "K" + key;
    }
}
