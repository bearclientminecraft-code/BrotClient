package dev.brot.setting;

import com.google.gson.JsonElement;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
    public final String name;
    protected T value;
    public Supplier<Boolean> visible = () -> true;
    public Consumer<T> onChange = v -> {};
    /** Tooltip text shown in the ClickGUI when the row is hovered. */
    public String description = "";

    protected Setting(String name, T def) { this.name = name; this.value = def; }

    public T get() { return value; }
    public void set(T v) { this.value = v; onChange.accept(v); }
    /** False for settings that mirror state stored elsewhere (e.g. the module bind) and must not be written to the config twice. */
    public boolean persistent() { return true; }
    public boolean isVisible() { return visible.get(); }
    public Setting<T> visibleWhen(Supplier<Boolean> s) { this.visible = s; return this; }

    public abstract JsonElement toJson();
    public abstract void fromJson(JsonElement e);
}
