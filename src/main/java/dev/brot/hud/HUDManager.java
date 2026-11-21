package dev.brot.hud;

import dev.brot.module.*;
import dev.brot.module.Module;
import dev.brot.render.GUIRenderer;
import dev.brot.setting.BooleanSetting;
import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class HUDManager {
    private HUDManager() {}

    public static final class MasterModule extends Module {
        public final BooleanSetting watermark = add(new BooleanSetting("Watermark", true),
            "Info bar: name, FPS, ping, time, coordinates, TPS and speed");
        MasterModule() { super("HUD", "Show HUD elements", Category.HUD); restoreEnabled(true); }
    }
    public static final MasterModule MASTER = new MasterModule();
    private static final List<HudElement> ELEMENTS = new ArrayList<>();

    public static void register(HudElement... e) { ELEMENTS.addAll(List.of(e)); ModuleManager.register(e); }
    public static List<HudElement> elements() { return ELEMENTS; }

    public static void render(DrawContext ctx) {
        if (!MASTER.isEnabled()) return;
        int sw = MinecraftClient.getInstance().getWindow().getScaledWidth();
        for (HudElement e : ELEMENTS) {
            e.layoutDefault(sw);
            if (e.shown()) renderOne(ctx, e);
        }
    }

    public static void renderOne(DrawContext ctx, HudElement e) {
        float prev = GUIRenderer.globalAlpha;
        GUIRenderer.globalAlpha = prev * e.opacity.f();
        var m = ctx.getMatrices();   // Matrix3x2fStack since 1.21.6 (verify)
        m.pushMatrix();
        m.translate(e.x, e.y);
        m.scale(e.scale.f(), e.scale.f());
        e.draw(ctx);
        m.popMatrix();
        GUIRenderer.globalAlpha = prev;
    }

    public static int[] bounds(HudElement e) {
        return new int[]{e.x, e.y, Math.round(e.width() * e.scale.f()), Math.round(e.height() * e.scale.f())};
    }
    public static void clamp(HudElement e) {
        var w = MinecraftClient.getInstance().getWindow();
        int[] b = bounds(e);
        e.x = Math.max(0, Math.min(w.getScaledWidth() - b[2], e.x));
        e.y = Math.max(0, Math.min(w.getScaledHeight() - b[3], e.y));
    }
}
