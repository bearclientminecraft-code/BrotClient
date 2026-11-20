package dev.brot.setting;

import dev.brot.module.Module;
import java.util.List;

/** Helper facade: visible-settings filtering for the GUI. */
public final class SettingManager {
    private SettingManager() {}
    public static List<Setting<?>> visible(Module m) {
        return m.settings().stream().filter(Setting::isVisible).toList();
    }
}
