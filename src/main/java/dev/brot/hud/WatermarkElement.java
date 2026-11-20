package dev.brot.hud;

import dev.brot.render.*;
import dev.brot.setting.BooleanSetting;
import dev.brot.theme.ThemeManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Compact info bar: accent logo + "BROT", then optional segments (name / fps / ping) separated by thin dividers. */
public class WatermarkElement extends HudElement {
    private final BooleanSetting showName = add(new BooleanSetting("Show Name", true).asCheckbox());
    private final BooleanSetting showFps = add(new BooleanSetting("Show FPS", true).asCheckbox());
    private final BooleanSetting showPing = add(new BooleanSetting("Show Ping", true).asCheckbox());
    private static final int H = 18, PAD = 7, GAP = 6;

    public WatermarkElement() { super("Watermark", 6, 6); }

    private List<String> segments() {
        var mc = MinecraftClient.getInstance();
        List<String> out = new ArrayList<>();
        if (showName.get() && mc.getSession() != null) out.add(mc.getSession().getUsername());
        if (showFps.get()) out.add(mc.getCurrentFps() + " fps");
        if (showPing.get()) out.add(ping(mc) + " ms");
        return out;
    }
    private static int ping(MinecraftClient mc) {
        if (mc.player == null || mc.getNetworkHandler() == null) return 0;
        var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());   // null in singleplayer (verify)
        return e == null ? 0 : e.getLatency();
    }

    @Override public int width() {
        int w = PAD + 8 + 5 + FontRenderer.width("BROT") + PAD;
        for (String s : segments()) w += GAP * 2 + 1 + FontRenderer.width(s);
        return w;
    }
    @Override public int height() { return H; }

    @Override public void draw(DrawContext c) {
        int w = width();
        GUIRenderer.shadow(c, 0, 0, w, H, 6, 8, 0x000000);
        GUIRenderer.roundBorder(c, 0, 0, w, H, 6, GUIRenderer.alpha(ThemeManager.panel(), 0.92f), ThemeManager.border());
        int x = PAD;
        GUIRenderer.round(c, x, 5, 8, 8, 3, ThemeManager.accent());
        x += 8 + 5;
        FontRenderer.draw(c, "BROT", x, 5, ThemeManager.text());
        x += FontRenderer.width("BROT");
        for (String s : segments()) {
            x += GAP;
            GUIRenderer.rect(c, x, 5, 1, 8, ThemeManager.border());
            x += 1 + GAP;
            FontRenderer.draw(c, s, x, 5, ThemeManager.textSecondary());
            x += FontRenderer.width(s);
        }
    }
}
