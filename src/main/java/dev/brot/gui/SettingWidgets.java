package dev.brot.gui;

import dev.brot.module.KeybindManager;
import dev.brot.render.*;
import dev.brot.setting.*;
import dev.brot.theme.Theme;
import dev.brot.theme.ThemeManager;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;

/** Draws and hit-tests one setting row. Adding a new Setting type = one branch here, no change to ClickGUI. */
public final class SettingWidgets {
    private SettingWidgets() {}
    public static final int GAP = 4;
    /** Key setting currently waiting for a key press (owned by the open ClickGUI). */
    public static KeySetting listening;
    private static final int CHIP_H = 12, TILE_H = 30, TILE_GAP = 4;

    /** Tile i of the theme grid (2 columns) -> {x, y, w}. */
    private static int[] tile(int i, int x, int y, int w) {
        int tw = (w - TILE_GAP) / 2;
        return new int[]{x + (i % 2) * (tw + TILE_GAP), y + 14 + (i / 2) * (TILE_H + TILE_GAP), tw};
    }

    public interface ChipVisitor { void chip(String mode, int cx, int cy, int cw); }

    public static int height(Setting<?> s, int w) {
        if (s instanceof KeySetting) return 14;
        if (s instanceof BooleanSetting) return 12;
        if (s instanceof NumberSetting) return 22;
        if (s instanceof ModeSetting m) return 12 + chipRows(m, w) * (CHIP_H + 2);
        if (s instanceof ThemeSetting t) return 14 + ((t.themes().size() + 1) / 2) * (TILE_H + TILE_GAP);
        return 12;
    }

    private static int chipRows(ModeSetting m, int w) { int[] r = {1}; chips(m, w, (n, x, y, cw) -> r[0] = y / (CHIP_H + 2) + 1); return r[0]; }

    private static void chips(ModeSetting m, int w, ChipVisitor v) {
        int cx = 0, cy = 0;
        for (String n : m.modes) {
            int cw = FontRenderer.width(n) + 8;
            if (cx + cw > w && cx > 0) { cx = 0; cy += CHIP_H + 2; }
            v.chip(n, cx, cy, cw);
            cx += cw + 3;
        }
    }

    public static void draw(DrawContext c, Setting<?> s, int x, int y, int w, int mx, int my) {
        int acc = ThemeManager.accent(), label = ThemeManager.textSecondary(), txt = ThemeManager.text();
        if (s instanceof BooleanSetting b) {
            FontRenderer.draw(c, b.name, x, y + 2, label);
            if (b.checkbox) {   // checkbox: 10px rounded box, inner mark when checked
                GUIRenderer.roundBorder(c, x + w - 10, y + 1, 10, 10, 3, b.get() ? acc : ThemeManager.hover(), b.get() ? acc : ThemeManager.border());
                if (b.get()) GUIRenderer.round(c, x + w - 7, y + 4, 4, 4, 1, ThemeManager.onAccent());
            } else {
                GUIRenderer.round(c, x + w - 16, y + 2, 16, 8, 4, b.get() ? acc : ThemeManager.border());
                GUIRenderer.round(c, x + w - 15 + (b.get() ? 8 : 0), y + 3, 6, 6, 3, b.get() ? ThemeManager.onAccent() : label);
            }
        } else if (s instanceof NumberSetting n) {
            FontRenderer.draw(c, n.name, x, y, label);
            String v = n.step >= 1 ? String.valueOf(n.i()) : String.format(Locale.ROOT, n.step >= 0.1 ? "%.1f" : "%.2f", n.get());
            if (!n.unit.isEmpty()) v += " " + n.unit;
            FontRenderer.draw(c, v, x + w - FontRenderer.width(v), y, txt);
            GUIRenderer.round(c, x, y + 13, w, 4, 2, ThemeManager.border());
            int fw = Math.max(4, (int) (w * n.ratio()));
            GUIRenderer.round(c, x, y + 13, fw, 4, 2, acc);
            GUIRenderer.round(c, x + fw - 3, y + 12, 6, 6, 3, ThemeManager.onAccent());
        } else if (s instanceof ModeSetting m) {
            FontRenderer.draw(c, m.name, x, y, label);
            chips(m, w, (n, cx, cy, cw) -> {
                boolean sel = m.is(n);
                GUIRenderer.round(c, x + cx, y + 11 + cy, cw, CHIP_H, 4, sel ? ThemeManager.active() : ThemeManager.hover());
                FontRenderer.draw(c, n, x + cx + 4, y + 13 + cy, sel ? txt : label);
            });
        } else if (s instanceof KeySetting ks) {
            FontRenderer.draw(c, ks.name, x, y + 2, label);
            boolean lis = listening == ks;
            String t = lis ? "..." : KeybindManager.name(ks.get(), ks.hotbar);
            int tw = Math.max(18, FontRenderer.width(t) + 10);
            GUIRenderer.roundBorder(c, x + w - tw, y, tw, 12, 4, lis ? ThemeManager.active() : ThemeManager.hover(),
                lis ? acc : ThemeManager.border());
            FontRenderer.drawCentered(c, t, x + w - tw / 2, y + 2, lis || ks.isSet() ? txt : label);
        } else if (s instanceof ThemeSetting ts) {
            FontRenderer.draw(c, ts.name, x, y, label);
            FontRenderer.draw(c, ThemeManager.selectedName(), x + w - FontRenderer.width(ThemeManager.selectedName()), y, txt);
            var list = ts.themes();
            for (int i = 0; i < list.size(); i++) {
                Theme t = list.get(i);
                int[] r = tile(i, x, y, w);
                boolean sel = t.name().equals(ts.get());
                boolean hov = mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + TILE_H;
                // mini preview: each tile is painted in its own palette, so the grid is a live comparison
                GUIRenderer.round(c, r[0], r[1], r[2], TILE_H, 6, t.background());
                GUIRenderer.outline(c, r[0], r[1], r[2], TILE_H, 6,
                    sel ? t.accent() : hov ? GUIRenderer.lerp(t.border(), t.accent(), 0.5f) : t.border(), 1f);
                FontRenderer.draw(c, t.name(), r[0] + 6, r[1] + 5, t.text());
                GUIRenderer.round(c, r[0] + 6, r[1] + 19, 14, 4, 2, t.accent());
                GUIRenderer.round(c, r[0] + 24, r[1] + 19, 20, 4, 2, GUIRenderer.alpha(t.textSecondary(), 0.6f));
            }
        }
    }

    /** Boolean/Mode clicks. Number sliders are handled by the screen (drag). */
    public static boolean click(Setting<?> s, int x, int y, int w, double mx, double my, int button) {
        if (s instanceof KeySetting ks) {
            if (button == 0) { listening = ks; return true; }
            if (button == 1) { ks.set(-1); return true; }   // clear (ignored for hotbar keys)
            return false;
        }
        if (button != 0) return false;
        if (s instanceof BooleanSetting b) { b.set(!b.get()); return true; }
        if (s instanceof ThemeSetting ts) {
            var list = ts.themes();
            for (int i = 0; i < list.size(); i++) {
                int[] r = tile(i, x, y, w);
                if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + TILE_H) { ts.set(list.get(i).name()); return true; }
            }
            return false;
        }
        if (s instanceof ModeSetting m) {
            boolean[] hit = {false};
            chips(m, w, (n, cx, cy, cw) -> {
                if (mx >= x + cx && mx < x + cx + cw && my >= y + 11 + cy && my < y + 11 + cy + CHIP_H) { m.set(n); hit[0] = true; }
            });
            return hit[0];
        }
        return false;
    }

    public static void drag(NumberSetting n, int x, int w, double mx) {
        n.setClamped(n.min + Math.max(0, Math.min(1, (mx - x) / w)) * (n.max - n.min));
    }
}
