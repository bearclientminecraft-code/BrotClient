package dev.brot.module;

import java.util.*;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static final Set<Integer> held = new HashSet<>();

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

    /** Polls binds (edge-triggered), only while no screen is open. */
    public static void tick(MinecraftClient mc) {
        if (mc.getWindow() == null) return;
        KeybindManager.beginTick();
        long h = mc.getWindow().getHandle();
        for (Module m : MODULES) {
            m.onTick();
            int k = m.bind();
            if (k < 0 || mc.currentScreen != null) continue;
            boolean down = GLFW.glfwGetKey(h, k) == GLFW.GLFW_PRESS;
            if (down && held.add(k)) m.toggle();
            else if (!down) held.remove(k);
        }
    }
}
