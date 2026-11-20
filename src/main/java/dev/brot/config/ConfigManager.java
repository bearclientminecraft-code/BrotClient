package dev.brot.config;

import com.google.gson.*;
import dev.brot.hud.HudElement;
import dev.brot.module.*;
import dev.brot.module.Module;
import dev.brot.setting.Setting;
import java.nio.file.*;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
    private ConfigManager() {}
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("brot.json"); }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : ModuleManager.all()) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.isEnabled());
                o.addProperty("bind", m.bind());
                if (m instanceof HudElement h) { o.addProperty("x", h.x); o.addProperty("y", h.y); }
                JsonObject s = new JsonObject();
                for (Setting<?> st : m.settings()) s.add(st.name, st.toJson());
                o.add("settings", s);
                root.add(m.name, o);
            }
            Files.writeString(file(), GSON.toJson(root));
        } catch (Exception ex) { System.err.println("[BROT] save failed: " + ex); }
    }

    public static void load() {
        try {
            if (!Files.exists(file())) return;
            JsonObject root = JsonParser.parseString(Files.readString(file())).getAsJsonObject();
            for (Module m : ModuleManager.all()) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("settings")) {
                    JsonObject s = o.getAsJsonObject("settings");
                    for (Setting<?> st : m.settings())
                        if (s.has(st.name)) try { st.fromJson(s.get(st.name)); } catch (Exception ignored) {}
                }
                if (o.has("bind")) m.setBind(o.get("bind").getAsInt());
                if (o.has("enabled")) m.restoreEnabled(o.get("enabled").getAsBoolean());
                if (m instanceof HudElement h) {
                    if (o.has("x")) h.x = o.get("x").getAsInt();
                    if (o.has("y")) h.y = o.get("y").getAsInt();
                }
            }
        } catch (Exception ex) { System.err.println("[BROT] load failed: " + ex); }
    }
}
