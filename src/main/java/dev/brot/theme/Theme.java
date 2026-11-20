package dev.brot.theme;

/**
 * One complete palette (all values opaque ARGB).
 * Only background + accent are hand-picked; every other role is derived from them by {@link #of},
 * so all themes keep identical contrast ratios and a consistent, flat look.
 */
public record Theme(String name, int background, int panel, int card, int border,
                    int text, int textSecondary, int accent, int hover, int active) {

    public static Theme of(String name, int bgRgb, int accentRgb) {
        int bg = 0xFF000000 | bgRgb, accent = 0xFF000000 | accentRgb;
        boolean dark = luma(bg) < 0.5f;
        int white = 0xFFFFFFFF, black = 0xFF000000;

        int panel  = dark ? mix(bg, white, 0.035f) : mix(bg, white, 0.55f);
        int card   = dark ? mix(bg, white, 0.070f) : mix(bg, white, 0.85f);
        int border = dark ? mix(bg, white, 0.110f) : mix(bg, black, 0.10f);
        int text   = dark ? mix(0xFFF4F4F6, accent, 0.06f) : mix(0xFF15151A, accent, 0.08f);
        int text2  = mix(bg, text, 0.55f);
        int hover  = mix(card, accent, 0.10f);
        int active = mix(card, accent, 0.20f);
        return new Theme(name, bg, panel, card, border, text, text2, accent, hover, active);
    }

    /** Readable colour for things drawn on top of the accent (toggle knob, avatar letter). */
    public int onAccent() { return luma(accent) > 0.6f ? 0xFF111114 : 0xFFFFFFFF; }

    public static Theme lerp(Theme a, Theme b, float t) {
        return new Theme(b.name,
            mix(a.background, b.background, t), mix(a.panel, b.panel, t), mix(a.card, b.card, t),
            mix(a.border, b.border, t), mix(a.text, b.text, t), mix(a.textSecondary, b.textSecondary, t),
            mix(a.accent, b.accent, t), mix(a.hover, b.hover, t), mix(a.active, b.active, t));
    }

    static int mix(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = 0;
        for (int s = 24; s >= 0; s -= 8) {
            int x = (c1 >>> s) & 255, y = (c2 >>> s) & 255;
            r |= (Math.round(x + (y - x) * t) & 255) << s;
        }
        return r;
    }

    static float luma(int argb) {
        return (0.2126f * (argb >> 16 & 255) + 0.7152f * (argb >> 8 & 255) + 0.0722f * (argb & 255)) / 255f;
    }
}
