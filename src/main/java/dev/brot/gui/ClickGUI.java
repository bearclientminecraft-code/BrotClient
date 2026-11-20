package dev.brot.gui;

import dev.brot.anim.AnimationManager;
import dev.brot.anim.AnimationManager.Anim;
import dev.brot.config.ConfigManager;
import dev.brot.hud.*;
import dev.brot.module.*;
import dev.brot.module.Module;
import dev.brot.render.*;
import dev.brot.setting.*;
import dev.brot.theme.ThemeManager;
import java.util.*;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    static final int W = 440, H = 290, SIDE = 112, PAD = 8, HEADER = 20, ROW = 20, COL_GAP = 8, CARD_GAP = 6;

    private record Card(Module m, int x, int y, int w, int h) {}
    private interface RowVisitor { void row(Setting<?> s, int x, int y, int w, int h); }

    private final CategoryManager cats = new CategoryManager();
    private final Map<Module, Anim> enabledAnim = new HashMap<>(), hoverAnim = new HashMap<>(), expandAnim = new HashMap<>();
    private final Map<Module, Boolean> expanded = new HashMap<>();   // settings open? (misc categories start open)
    private final Anim open = new Anim(0), page = new Anim(1), indicator = new Anim(0), scroll = new Anim(0);
    private boolean closing, searchFocus, shiftArmed;
    private float scrollTarget, contentH;
    private Module binding;
    private NumberSetting dragNum; private int dragX, dragW;
    private HudElement dragHud; private int dragOffX, dragOffY;

    public ClickGUI() {
        super(Text.literal("BROT"));
        open.to(1);
        indicator.value = indicator.target = rowY(cats.selected);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public void removed() { SettingWidgets.listening = null; ConfigManager.save(); }

    // ---------- geometry ----------
    private int x0() { return (width - W) / 2; }
    private int y0() { return (height - H) / 2; }
    private int rowY(Category c) {
        int i = CategoryManager.MAIN.indexOf(c);
        if (i >= 0) return 54 + i * 22;
        return 192 + CategoryManager.MISC.indexOf(c) * 22;
    }

    private List<Card> layout() {
        int vx = x0() + SIDE + PAD, vy = y0() + PAD + Math.round((1 - page.value) * 10);
        int vw = W - SIDE - 2 * PAD, cw = (vw - COL_GAP) / 2;
        int[] colY = {vy - Math.round(scroll.value), vy - Math.round(scroll.value)};
        List<Card> out = new ArrayList<>();
        for (Module m : cats.visibleModules()) {
            int h = cardHeight(m, cw - 16);
            int col = colY[0] <= colY[1] ? 0 : 1;
            out.add(new Card(m, vx + col * (cw + COL_GAP), colY[col], cw, h));
            colY[col] += h + CARD_GAP;
        }
        contentH = Math.max(colY[0], colY[1]) - vy + Math.round(scroll.value);
        return out;
    }

    private int cardHeight(Module m, int rowW) {
        int h = HEADER + 2;
        var vis = SettingManager.visible(m);
        if (!vis.isEmpty()) {
            int sh = 4;
            for (var s : vis) sh += SettingWidgets.height(s, rowW) + SettingWidgets.GAP;
            h += Math.round(sh * exp(m));
        }
        return h;
    }

    private float exp(Module m) { Anim a = expandAnim.get(m); return a == null ? (m.category.misc ? 1f : 0f) : a.value; }
    private boolean isExpanded(Module m) { return expanded.getOrDefault(m, m.category.misc); }

    private void rows(Card k, RowVisitor v) {
        int y = k.y + HEADER + 4;
        for (var s : SettingManager.visible(k.m)) {
            int h = SettingWidgets.height(s, k.w - 16);
            v.row(s, k.x + 8, y, k.w - 16, h);
            y += h + SettingWidgets.GAP;
        }
    }

    private void select(Category c) {
        if (c == cats.selected) return;
        cats.selected = c; cats.search = ""; scrollTarget = 0; scroll.value = scroll.target = 0;
        page.value = 0; page.to(1); indicator.to(rowY(c));
    }

    // ---------- rendering ----------
    @Override public void render(DrawContext c, int mx, int my, float delta) {
        AnimationManager.frame();
        // Right Shift closes only after it was released once (the press that opened the GUI must not close it)
        if (!shiftArmed && GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) != GLFW.GLFW_PRESS) shiftArmed = true;
        open.update(); page.update(); indicator.update();
        for (Module m : ModuleManager.all()) {   // once per frame: smooth open/close of each card's settings
            Anim a = expandAnim.computeIfAbsent(m, mm -> new Anim(mm.category.misc ? 1 : 0));
            a.to(isExpanded(m) ? 1 : 0); a.update();
        }
        scrollTarget = Math.max(0, Math.min(scrollTarget, Math.max(0, contentH - (H - 2 * PAD))));
        scroll.to(scrollTarget); scroll.update();
        if (closing && open.value <= 0.01f) { close(); return; }

        GUIRenderer.globalAlpha = 1f;
        super.render(c, mx, my, delta);                 // vanilla background blur
        GUIRenderer.globalAlpha = open.value;
        GUIRenderer.rect(c, 0, 0, width, height, BlurRenderer.EXTRA_DIM);

        int x0 = x0(), y0 = y0(), r = ThemeManager.rounding() + 2;
        float pa = ThemeManager.panelAlpha();
        GUIRenderer.shadow(c, x0, y0, W, H, r, 16, 0x000000);
        GUIRenderer.roundBorder(c, x0, y0, W, H, r, GUIRenderer.alpha(ThemeManager.panel(), pa), ThemeManager.border());
        GUIRenderer.roundBorder(c, x0 + 5, y0 + 5, SIDE - 8, H - 10, r - 2,
            GUIRenderer.alpha(ThemeManager.bg(), pa), GUIRenderer.alpha(ThemeManager.border(), 0.6f));

        drawSidebar(c, mx, my);

        GUIRenderer.globalAlpha = open.value * page.value;
        int vy = y0 + PAD, vh = H - 2 * PAD;
        List<Card> cards = layout();
        GUIRenderer.scissor(c, x0 + SIDE + 2, vy, W - SIDE - 2, vh);
        if (cards.isEmpty()) FontRenderer.drawCentered(c, "Nothing found", x0 + SIDE + (W - SIDE) / 2, y0 + H / 2, ThemeManager.textSecondary());
        Card hover = null;
        for (Card k : cards) {
            if (k.y + k.h < vy || k.y > vy + vh) continue;
            drawCard(c, k, mx, my);
            if (mx >= k.x && mx < k.x + k.w && my >= k.y && my < k.y + HEADER && my >= vy && my < vy + vh) hover = k;
        }
        GUIRenderer.endScissor(c);
        if (hover != null && !hover.m.description.isEmpty()) {
            int tw = FontRenderer.width(hover.m.description) + 8;
            GUIRenderer.roundBorder(c, mx + 8, my + 8, tw, 14, 4, GUIRenderer.alpha(ThemeManager.card(), 0.95f), ThemeManager.border());
            FontRenderer.draw(c, hover.m.description, mx + 12, my + 11, ThemeManager.text());
        }

        GUIRenderer.globalAlpha = open.value;
        if (cats.selected == Category.HUD) drawHudOutlines(c);
        GUIRenderer.globalAlpha = 1f;
    }

    private void drawSidebar(DrawContext c, int mx, int my) {
        int x0 = x0(), y0 = y0(), acc = ThemeManager.accent(), txt = ThemeManager.text(), dim = ThemeManager.textSecondary();
        GUIRenderer.round(c, x0 + 12, y0 + 13, 9, 9, 3, acc);
        FontRenderer.draw(c, "BROT", x0 + 26, y0 + 14, txt);

        // search
        GUIRenderer.roundBorder(c, x0 + 8, y0 + 30, SIDE - 16, 16, 5, ThemeManager.card(), searchFocus ? acc : ThemeManager.border());
        String st = cats.search.isEmpty() && !searchFocus ? "Search" : cats.search + (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        FontRenderer.draw(c, st, x0 + 14, y0 + 35, cats.search.isEmpty() ? dim : txt);

        // selected indicator
        if (cats.search.isEmpty())
        {
            int iy = y0 + Math.round(indicator.value);
            GUIRenderer.round(c, x0 + 8, iy, SIDE - 16, ROW, 6, ThemeManager.active());
            GUIRenderer.round(c, x0 + 8, iy + 5, 2, ROW - 10, 1, acc);
        }

        for (Category cat : CategoryManager.MAIN) drawCatRow(c, cat);
        int sepY = y0 + 54 + 5 * 22 + 2;
        GUIRenderer.rect(c, x0 + 12, sepY, SIDE - 24, 1, ThemeManager.border());
        icon(c, "misc", x0 + 14, sepY + 7, 10, dim);
        FontRenderer.draw(c, "MISC", x0 + 28, sepY + 8, dim);
        for (Category cat : CategoryManager.MISC) drawCatRow(c, cat);

        // footer
        String name = client.getSession().getUsername();
        GUIRenderer.round(c, x0 + 10, y0 + H - 32, 22, 22, 6, acc);
        FontRenderer.drawCentered(c, name.substring(0, 1).toUpperCase(Locale.ROOT), x0 + 21, y0 + H - 25, ThemeManager.onAccent());
        FontRenderer.draw(c, name, x0 + 37, y0 + H - 29, txt);
        FontRenderer.draw(c, "BROT v0.1", x0 + 37, y0 + H - 18, dim);
    }

    private void drawCatRow(DrawContext c, Category cat) {
        int x0 = x0(), y = y0() + rowY(cat);
        boolean sel = cat == cats.selected && cats.search.isEmpty();
        int col = sel ? ThemeManager.text() : ThemeManager.textSecondary();
        icon(c, cat.icon, x0 + 16, y + 4, 12, col);
        FontRenderer.draw(c, cat.label, x0 + 34, y + 6, col);
    }

    private void icon(DrawContext c, String name, int x, int y, int size, int color) {
        // 13-arg drawTexture (pipeline, id, x, y, u, v, w, h, regionW, regionH, texW, texH, color): verify in 1.21.11
        c.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of("brot", "textures/gui/" + name + ".png"),
            x, y, 0f, 0f, size, size, 48, 48, 48, 48, GUIRenderer.a(color));
    }

    private void drawCard(DrawContext c, Card k, int mx, int my) {
        Module m = k.m;
        Anim an = enabledAnim.computeIfAbsent(m, mm -> new Anim(mm.isEnabled() ? 1 : 0));
        an.to(m.isEnabled() ? 1 : 0); an.update();
        int acc = ThemeManager.accent(), dim = ThemeManager.textSecondary();
        Anim hv = hoverAnim.computeIfAbsent(m, mm -> new Anim(0));
        hv.to(mx >= k.x && mx < k.x + k.w && my >= k.y && my < k.y + HEADER ? 1 : 0); hv.update();
        int border = GUIRenderer.lerp(ThemeManager.border(), GUIRenderer.alpha(acc, 0.8f), an.value);
        int fill = GUIRenderer.lerp(GUIRenderer.lerp(ThemeManager.card(), ThemeManager.hover(), hv.value), ThemeManager.active(), an.value * 0.5f);
        GUIRenderer.roundBorder(c, k.x, k.y, k.w, k.h, Math.max(2, ThemeManager.rounding() - 2), fill, border);

        FontRenderer.draw(c, m.name, k.x + 8, k.y + 6, GUIRenderer.lerp(dim, ThemeManager.text(), an.value));
        int right = k.x + k.w - 8;
        if (m.category != Category.THEMES) {
            GUIRenderer.round(c, right - 16, k.y + 6, 16, 8, 4, GUIRenderer.lerp(ThemeManager.border(), acc, an.value));
            GUIRenderer.round(c, right - 15 + Math.round(an.value * 8), k.y + 7, 6, 6, 3, GUIRenderer.lerp(dim, ThemeManager.onAccent(), an.value));
            right -= 22;
        }
        String bt = binding == m ? "[...]" : (m.bind() >= 0 ? "[" + keyName(m.bind()) + "]" : "");
        if (!m.settings().isEmpty()) {   // expand marker (right click on the header toggles it)
            String ex = isExpanded(m) ? "-" : "+";
            FontRenderer.draw(c, ex, right - FontRenderer.width(ex), k.y + 6, dim);
            right -= 12;
        }
        if (!bt.isEmpty()) FontRenderer.draw(c, bt, right - FontRenderer.width(bt), k.y + 6, dim);

        if (!m.settings().isEmpty() && exp(m) > 0.02f) {
            GUIRenderer.scissor(c, k.x, k.y + HEADER, k.w, k.h - HEADER);   // clip rows while the card grows/shrinks
            GUIRenderer.rect(c, k.x + 6, k.y + HEADER, k.w - 12, 1, GUIRenderer.alpha(ThemeManager.border(), 0.8f));
            rows(k, (s, x, y, w, h) -> SettingWidgets.draw(c, s, x, y, w, mx, my));
            GUIRenderer.endScissor(c);
        }
    }

    private void drawHudOutlines(DrawContext c) {
        for (HudElement e : HUDManager.elements()) {
            if (!e.isEnabled()) continue;
            int[] b = HUDManager.bounds(e);
            int col = e == dragHud ? ThemeManager.text() : GUIRenderer.alpha(ThemeManager.accent(), 0.9f);
            GUIRenderer.outline(c, b[0] - 2, b[1] - 2, b[2] + 4, b[3] + 4, 5, col, 1.5f);
        }
    }

    private static String keyName(int k) { return KeybindManager.name(k, false); }

    // ---------- input (signatures target 1.21.9+ input events: VERIFY) ----------
    @Override public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y(); int b = click.button();
        if (closing) return true;
        if (SettingWidgets.listening != null) {            // next click binds that mouse button (Esc cancels)
            var ks = SettingWidgets.listening;
            SettingWidgets.listening = null;
            ks.set(KeybindManager.mouse(b));
            ConfigManager.save();
            return true;
        }
        if (binding != null) { binding.setBind(KeybindManager.mouse(b)); binding = null; ConfigManager.save(); return true; }
        int x0 = x0(), y0 = y0();
        boolean inPanel = mx >= x0 && mx < x0 + W && my >= y0 && my < y0 + H;

        if (!inPanel) {
            if (cats.selected == Category.HUD && b == 0) {
                List<HudElement> els = new ArrayList<>(HUDManager.elements());
                Collections.reverse(els);
                for (HudElement e : els) {
                    int[] bb = HUDManager.bounds(e);
                    if (e.isEnabled() && mx >= bb[0] && mx < bb[0] + bb[2] && my >= bb[1] && my < bb[1] + bb[3]) {
                        dragHud = e; dragOffX = (int) mx - e.x; dragOffY = (int) my - e.y; return true;
                    }
                }
            }
            return true;
        }

        searchFocus = mx >= x0 + 8 && mx < x0 + SIDE - 8 && my >= y0 + 30 && my < y0 + 46;
        if (mx < x0 + SIDE) {
            for (Category cat : Category.values())
                if (mx >= x0 + 8 && mx < x0 + SIDE - 8 && my >= y0 + rowY(cat) && my < y0 + rowY(cat) + ROW) { cats.search = ""; select(cat); }
            return true;
        }

        int vy = y0 + PAD, vh = H - 2 * PAD;
        if (my < vy || my >= vy + vh) return true;
        binding = null;
        SettingWidgets.listening = null;   // any click cancels a pending key capture (a click on a key chip re-arms it below)
        for (Card k : layout()) {
            if (mx < k.x || mx >= k.x + k.w || my < k.y || my >= k.y + k.h) continue;
            if (my < k.y + HEADER) {
                if (b == 0 && k.m.category != Category.THEMES) k.m.toggle();
                else if (b == 1 && !k.m.settings().isEmpty()) expanded.put(k.m, !isExpanded(k.m));
                else if (b == 2) binding = k.m;   // middle click: module toggle key
                return true;
            }
            if (exp(k.m) > 0.9f) rows(k, (s, x, y, w, h) -> {
                if (my < y || my >= y + h || mx < x || mx >= x + w) return;
                if (s instanceof NumberSetting n && b == 0) { dragNum = n; dragX = x; dragW = w; SettingWidgets.drag(n, x, w, mx); }
                else if (SettingWidgets.click(s, x, y, w, mx, my, b) && s instanceof KeySetting ks && b == 1) ConfigManager.save();
            });
            return true;
        }
        return true;
    }

    @Override public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragNum != null) SettingWidgets.drag(dragNum, dragX, dragW, click.x());
        if (dragHud != null) {
            dragHud.x = (int) click.x() - dragOffX; dragHud.y = (int) click.y() - dragOffY;
            HUDManager.clamp(dragHud);
        }
        return true;
    }

    @Override public boolean mouseReleased(Click click) {
        if (dragNum != null || dragHud != null) ConfigManager.save();
        dragNum = null; dragHud = null;
        return true;
    }

    @Override public boolean mouseScrolled(double mx, double my, double h, double v) {
        scrollTarget -= (float) v * 24;
        return true;
    }

    @Override public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (SettingWidgets.listening != null) {
            var ks = SettingWidgets.listening;
            SettingWidgets.listening = null;
            if (key != GLFW.GLFW_KEY_ESCAPE) {                       // Esc cancels, Backspace/Delete clears, any other key binds
                ks.set(key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE ? -1 : key);
                ConfigManager.save();
            }
            return true;
        }
        if (binding != null) {
            binding.setBind(key == GLFW.GLFW_KEY_ESCAPE ? -1 : key);
            binding = null; ConfigManager.save(); return true;
        }
        if (searchFocus) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !cats.search.isEmpty()) { cats.search = cats.search.substring(0, cats.search.length() - 1); scrollTarget = 0; return true; }
            if (key == GLFW.GLFW_KEY_ENTER) { searchFocus = false; return true; }
        }
        switch (key) {   // keyboard scrolling (also works on key repeat)
            case GLFW.GLFW_KEY_UP -> { scrollTarget -= 28; return true; }
            case GLFW.GLFW_KEY_DOWN -> { scrollTarget += 28; return true; }
            case GLFW.GLFW_KEY_PAGE_UP -> { scrollTarget -= 140; return true; }
            case GLFW.GLFW_KEY_PAGE_DOWN -> { scrollTarget += 140; return true; }
            case GLFW.GLFW_KEY_HOME -> { scrollTarget = 0; return true; }
            case GLFW.GLFW_KEY_END -> { scrollTarget = Float.MAX_VALUE / 4; return true; }   // clamped in render()
            default -> {}
        }
        if (key == GLFW.GLFW_KEY_ESCAPE || (key == GLFW.GLFW_KEY_RIGHT_SHIFT && shiftArmed)) { closing = true; open.to(0); }
        return true;
    }

    @Override public boolean charTyped(CharInput input) {
        if (searchFocus && cats.search.length() < 16) {
            int cp = input.codepoint();
            if (cp >= 32) { cats.search += new String(Character.toChars(cp)); scrollTarget = 0; }
        }
        return true;
    }
}
