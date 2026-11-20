package dev.brot.render;

import dev.brot.theme.ThemeManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/**
 * Shape drawing. Primary path: SDF shader (anti-aliased, any radius, gradients via vertex colours).
 * Fallback (Theme -> "Smooth shader" off, or pipeline failed to register): 1px strips, not anti-aliased.
 */
public final class GUIRenderer {
    private GUIRenderer() {}
    /** Global alpha multiplier (open/close, page transitions, HUD opacity). */
    public static float globalAlpha = 1f;
    private static ScreenRect scissorRect;   // our own tracking, DrawContext's scissor stack is private
    private static final java.util.ArrayList<ScreenRect> scissorStack = new java.util.ArrayList<>();

    public static int a(int argb) {
        int al = Math.round(((argb >>> 24) & 255) * globalAlpha);
        return (al << 24) | (argb & 0xFFFFFF);
    }
    public static int alpha(int argb, float f) {
        return (Math.round(((argb >>> 24) & 255) * f) << 24) | (argb & 0xFFFFFF);
    }
    public static int lerp(int c1, int c2, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = 0;
        for (int s = 24; s >= 0; s -= 8) {
            int x = (c1 >>> s) & 255, y = (c2 >>> s) & 255;
            r |= (Math.round(x + (y - x) * t) & 255) << s;
        }
        return r;
    }

    private static boolean shader() { return ShapePipelines.ready && ThemeManager.smooth(); }

    // ---------- public API ----------
    public static void rect(DrawContext c, int x, int y, int w, int h, int color) { c.fill(x, y, x + w, y + h, a(color)); }

    public static void round(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (!shader()) { stripRound(c, x, y, w, h, r, color); return; }
        color = a(color);
        submit(c, x, y, w, h, r, 0, 0, 0, color, color, color, color);
    }

    /** Fill plus an anti-aliased 1px ring on top. */
    public static void roundBorder(DrawContext c, int x, int y, int w, int h, int r, int fill, int border) {
        round(c, x, y, w, h, r, fill);
        outline(c, x, y, w, h, r, border, 1f);
    }

    public static void outline(DrawContext c, int x, int y, int w, int h, int r, int color, float width) {
        if (!shader()) { stripRound(c, x, y, w, 1, 0, color); stripRound(c, x, y + h - 1, w, 1, 0, color);
                         stripRound(c, x, y, 1, h, 0, color); stripRound(c, x + w - 1, y, 1, h, 0, color); return; }
        color = a(color);
        submit(c, x, y, w, h, r, 1, width, 0, color, color, color, color);
    }

    public static void roundGradient(DrawContext c, int x, int y, int w, int h, int r, int c1, int c2, boolean horizontal) {
        if (!shader()) { stripGradient(c, x, y, w, h, r, c1, c2, horizontal); return; }
        c1 = a(c1); c2 = a(c2);
        if (horizontal) submit(c, x, y, w, h, r, 0, 0, 0, c1, c1, c2, c2);   // TL BL BR TR
        else            submit(c, x, y, w, h, r, 0, 0, 0, c1, c2, c2, c1);
    }

    /** Soft shadow behind a rounded rect (single SDF quad). */
    public static void shadow(DrawContext c, int x, int y, int w, int h, int r, int size, int rgb) {
        if (!shader()) return;
        int col = a((70 << 24) | (rgb & 0xFFFFFF));
        submit(c, x, y + 3, w, h, r, 2, 0, size, col, col, col, col);
    }

    public static void scissor(DrawContext c, int x, int y, int w, int h) {
        scissorStack.add(scissorRect);   // nested scissors: remember the outer one (may be null)
        ScreenRect r = new ScreenRect(x, y, w, h);
        if (scissorRect != null) { ScreenRect i = scissorRect.intersection(r); r = i != null ? i : new ScreenRect(0, 0, 0, 0); }
        c.enableScissor(x, y, x + w, y + h);
        scissorRect = r;
    }
    public static void endScissor(DrawContext c) {
        c.disableScissor();
        scissorRect = scissorStack.isEmpty() ? null : scissorStack.remove(scissorStack.size() - 1);
    }

    // ---------- shader path ----------
    private static void submit(DrawContext c, float x, float y, float w, float h, float r, int mode, float border, float soft,
                               int cTL, int cBL, int cBR, int cTR) {
        float pad = mode == 2 ? Math.max(soft, 1f) : 1f;
        Matrix3x2f pose = new Matrix3x2f(c.getMatrices());
        Vector2f p = new Vector2f();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float[][] corners = {{x - pad, y - pad}, {x + w + pad, y - pad}, {x - pad, y + h + pad}, {x + w + pad, y + h + pad}};
        for (float[] k : corners) {
            pose.transformPosition(k[0], k[1], p);
            minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
        }
        ScreenRect bounds = new ScreenRect((int) Math.floor(minX), (int) Math.floor(minY),
            (int) Math.ceil(maxX - minX) + 1, (int) Math.ceil(maxY - minY) + 1);
        float rad = Math.max(0f, Math.min(r, Math.min(w, h) / 2f));
        c.state.addSimpleElement(new ShapeState(ShapePipelines.ROUND, TextureSetup.empty(), scissorRect, bounds, pose,
            x, y, w, h, rad, pad, mode, border, soft, cTL, cBL, cBR, cTR));   // c.state: widened by brot.accesswidener
    }

    // ---------- fallback path (aliased) ----------
    private static int inset(int r, int d) {
        if (d >= r) return 0;
        double dy = r - d - 0.5;
        return (int) Math.round(r - Math.sqrt(Math.max(0, (double) r * r - dy * dy)));
    }
    private static void stripRound(DrawContext c, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w, h) / 2); color = a(color);
        if (r <= 0) { c.fill(x, y, x + w, y + h, color); return; }
        for (int i = 0; i < r; i++) {
            int in = inset(r, i);
            c.fill(x + in, y + i, x + w - in, y + i + 1, color);
            c.fill(x + in, y + h - 1 - i, x + w - in, y + h - i, color);
        }
        c.fill(x, y + r, x + w, y + h - r, color);
    }
    private static void stripGradient(DrawContext c, int x, int y, int w, int h, int r, int c1, int c2, boolean hz) {
        r = Math.min(r, Math.min(w, h) / 2);
        for (int i = 0, n = hz ? w : h; i < n; i++) {
            int in = i < r ? inset(r, i) : (i >= n - r ? inset(r, n - 1 - i) : 0);
            int col = a(lerp(c1, c2, i / (float) Math.max(1, n - 1)));
            if (hz) c.fill(x + i, y + in, x + i + 1, y + h - in, col);
            else c.fill(x + in, y + i, x + w - in, y + i + 1, col);
        }
    }
}
