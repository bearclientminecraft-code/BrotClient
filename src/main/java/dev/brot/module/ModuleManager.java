package dev.brot.module;

import dev.brot.config.ConfigManager;
import dev.brot.setting.BindSetting;
import dev.brot.setting.KeySetting;
import dev.brot.setting.Setting;
import java.util.*;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {}

    public static void register(Module... ms) {
        for (Module m : ms) {
            if (byName(m.name) != null) throw new IllegalStateException("Duplicate module: " + m.name);
            MODULES.add(m);
        }
    }
    public static List<Module> all() { return Collections.unmodifiableList(MODULES); }
    public static List<Module> in(Category c) { return MODULES.stream().filter(m -> m.category == c).toList(); }
    public static Module byName(String n) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(n)) return m;
        return null;
    }

    /** Once per rendered frame. */
    public static void frame() { for (Module m : MODULES) if (m.isEnabled()) m.onFrame(); }

    /**
     * Who else uses `key`? Checks every module's Bind Key, every non-hotbar key setting (Activate Key etc.) and the
     * ClickGUI key. Returns a display label, or null when the key is free. `self` (the row being edited) is skipped.
     */
    public static String conflict(Setting<Integer> self, int key) {
        if (key < 0) return null;
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) return "ClickGUI";
        for (Module m : MODULES)
            for (Setting<?> s : m.settings()) {
                if (s == self) continue;
                if (s instanceof BindSetting b && b.get() == key) return m.name;
                if (s instanceof KeySetting k && !k.hotbar && k.get() == key) return m.name + " / " + k.name;
            }
        return null;
    }

    /** Polls binds (edge-triggered), only while no screen is open. Modules sharing one key all toggle (the GUI warns about it). */
    public static void tick(MinecraftClient mc) {
        if (mc.getWindow() == null) return;
        KeybindManager.beginTick();
        boolean toggled = false;
        for (Module m : MODULES) {
            m.onTick();
            int k = m.bind();
            if (k < 0 || mc.currentScreen != null) continue;
            if (KeybindManager.pressed(k)) { m.toggle(); toggled = true; }   // keyboard key or mouse button
        }
        if (toggled) ConfigManager.save();   // enabled state survives a crash / quit without opening the GUI
    }
}
