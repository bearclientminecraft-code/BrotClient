package dev.brot.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.brot.theme.Theme;
import dev.brot.theme.ThemeManager;
import dev.brot.theme.Themes;
import java.util.List;

/** Selected theme name, drawn as a grid of live previews (see SettingWidgets). */
public class ThemeSetting extends Setting<String> {
    public ThemeSetting(String name, String def) { super(name, def); }
    public List<Theme> themes() { return Themes.LIST; }

    @Override public void set(String v) { if (!v.equals(value)) super.set(v); }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    /** Config load: unknown names are ignored; applied instantly (no 250 ms fade at startup). */
    @Override public void fromJson(JsonElement e) {
        Theme t = Themes.byName(e.getAsString());
        if (t == null) return;
        value = t.name();
        ThemeManager.snap(t.name());
    }
}
