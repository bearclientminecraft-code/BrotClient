package dev.brot.hud;

import dev.brot.render.*;
import dev.brot.theme.ThemeManager;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Two centred rows of dark capsules, each "icon value | icon value | ...":
 *   row 1: name | FPS | server (icon only) | ping | clock
 *   row 2: coordinates | TPS | BPS
 * Shown/hidden by the "Watermark" toggle of the HUD module, so it has no card of its own.
 * Text is rebuilt every 50 ms (not per frame) and cached together with its widths.
 */
public class WatermarkElement extends HudElement {
    private static final int H = 17, PAD = 8, ICON = 8, ICON_GAP = 5, SEP_GAP = 7, ROW_GAP = 5, RADIUS = 7;
    private static final int BG = 0xC0121218, EDGE = 0x1FFFFFFF, TEXT = 0xFFF2F2F6, SEP = 0x1FFFFFFF;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    private record Seg(String icon, String text, int w) {}
    private List<Seg> row1 = List.of(), row2 = List.of();
    private int w1, w2;
    private long builtAt;

    public WatermarkElement() { super("WatermarkBar", -1, 4); }   // x = -1: centred on first frame (see layoutDefault)

    @Override public boolean shown() { return HUDManager.MASTER.watermark.get(); }
    @Override public boolean hiddenInGui() { return true; }
    @Override public void layoutDefault(int screenW) { if (x < 0) x = Math.max(0, (screenW - Math.round(width() * scale.f())) / 2); }

    private static Seg seg(String icon, String text) {
        return new Seg(icon, text, ICON + (text.isEmpty() ? 0 : ICON_GAP + FontRenderer.width(text)));
    }
    private static int rowWidth(List<Seg> r) {
        int w = PAD * 2 + (r.size() - 1) * (SEP_GAP * 2 + 1);
        for (Seg s : r) w += s.w();
        return w;
    }
    private static int ping(MinecraftClient mc) {
        if (mc.player == null || mc.getNetworkHandler() == null) return 0;
        var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());   // null in singleplayer (verify)
        return e == null ? 0 : e.getLatency();
    }

    private void refresh() {
        long now = System.nanoTime() / 1_000_000L;
        if (now - builtAt < 50 && !row1.isEmpty()) return;
        builtAt = now;
        var mc = MinecraftClient.getInstance();
        String name = mc.getSession() == null ? "" : mc.getSession().getUsername();
        String pos = mc.player == null ? "x 0 y 0 z 0"
            : "x " + mc.player.getBlockX() + " y " + mc.player.getBlockY() + " z " + mc.player.getBlockZ();
        row1 = List.of(seg("user", name), seg("fps", mc.getCurrentFps() + " FPS"), seg("server", ""),
            seg("cloud", ping(mc) + " ms"), seg("clock", LocalTime.now().format(CLOCK)));
        row2 = List.of(seg("pin", pos), seg("signal", String.format(Locale.ROOT, "%.1f TPS", HudStats.tps())),
            seg("gauge", String.format(Locale.ROOT, "%.1f BPS", HudStats.bps())));
        w1 = rowWidth(row1); w2 = rowWidth(row2);
    }

    @Override public int width() { refresh(); return Math.max(w1, w2); }
    @Override public int height() { return H * 2 + ROW_GAP; }

    @Override public void draw(DrawContext c) {
        refresh();
        int total = Math.max(w1, w2);
        drawRow(c, row1, (total - w1) / 2, 0, w1);
        drawRow(c, row2, (total - w2) / 2, H + ROW_GAP, w2);
    }

    private void drawRow(DrawContext c, List<Seg> row, int rx, int ry, int w) {
        GUIRenderer.roundBorder(c, rx, ry, w, H, RADIUS, BG, EDGE);
        int icon = GUIRenderer.alpha(ThemeManager.accent(), 0.7f), x = rx + PAD, ty = ry + (H - FontRenderer.height()) / 2 + 1;
        for (int i = 0; i < row.size(); i++) {
            Seg s = row.get(i);
            if (i > 0) {
                x += SEP_GAP;
                GUIRenderer.rect(c, x, ry + (H - 8) / 2, 1, 8, SEP);
                x += 1 + SEP_GAP;
            }
            // 13-arg drawTexture, same call as ClickGUI.icon (verify on 1.21.11)
            c.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of("brot", "textures/gui/wm/" + s.icon() + ".png"),
                x, ry + (H - ICON) / 2, 0f, 0f, ICON, ICON, 48, 48, 48, 48, GUIRenderer.a(icon));
            if (!s.text().isEmpty()) FontRenderer.draw(c, s.text(), x + ICON + ICON_GAP, ty, TEXT);
            x += s.w();
        }
    }
}
