package dev.brot.module;

import java.util.*;
import net.minecraft.client.MinecraftClient;

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

    /** Once per rendered frame. */
    public static void frame() { for (Module m : MODULES) if (m.isEnabled()) m.onFrame(); }

    /** Polls binds (edge-triggered), only while no screen is open. */
    public static void tick(MinecraftClient mc) {
        if (mc.getWindow() == null) return;
        KeybindManager.beginTick();
        for (Module m : MODULES) {
            m.onTick();
            int k = m.bind();
            if (k < 0 || mc.currentScreen != null) continue;
            boolean down = KeybindManager.down(k);   // keyboard key or mouse button
            if (down && held.add(k)) m.toggle();
            else if (!down) held.remove(k);
        }
    }
}
