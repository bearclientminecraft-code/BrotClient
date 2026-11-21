package dev.brot.module;

import dev.brot.setting.BindSetting;
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
        if (category != Category.THEMES) settings.add(new BindSetting(this));   // every module gets a Bind Key row
    }

    protected <S extends Setting<?>> S add(S s) { settings.add(s); return s; }
    protected <S extends Setting<?>> S add(S s, String description) { s.description = description; return add(s); }
    public List<Setting<?>> settings() { return settings; }

    /** Modules that are driven by another module's setting have no card in the ClickGUI. */
    public boolean hiddenInGui() { return false; }

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
    /** Called once per rendered frame while enabled (smooth camera work etc.). */
    public void onFrame() {}
}
