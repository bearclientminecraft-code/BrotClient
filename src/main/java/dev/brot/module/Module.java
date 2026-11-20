package dev.brot.module;

import dev.brot.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public class Module {
    public final String name, description;
    public final Category category;
    private boolean enabled;
    private int bind = -1; // GLFW key, -1 = none
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected <S extends Setting<?>> S add(S s) { settings.add(s); return s; }
    public List<Setting<?>> settings() { return settings; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) {
        if (v == enabled) return;
        enabled = v;
        if (v) onEnable(); else onDisable();
    }
    public void toggle() { setEnabled(!enabled); }
    /** Restore state (config load / defaults). */
    public void restoreEnabled(boolean v) { enabled = v; if (v) onEnable(); }

    public int bind() { return bind; }
    public void setBind(int key) { bind = key; }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
}
