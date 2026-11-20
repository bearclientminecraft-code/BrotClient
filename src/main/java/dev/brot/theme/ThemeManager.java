package dev.brot.theme;

import dev.brot.config.ConfigManager;
import dev.brot.module.Category;
import dev.brot.module.Module;
import dev.brot.setting.*;

public final class ThemeManager {
    private ThemeManager() {}

    public static final long TRANSITION_MS = 250;

    public static final class ThemeModule extends Module {
        public final ThemeSetting theme = add(new ThemeSetting("Theme", Themes.DEFAULT.name()));
        public final NumberSetting panelAlpha = add(new NumberSetting("Panel opacity", 0.92, 0.3, 1.0, 0.01));
        public final NumberSetting rounding = add(new NumberSetting("Rounding", 8, 0, 14, 1));
        public final BooleanSetting smooth = add(new BooleanSetting("Smooth shader", true));
        public final NumberSetting animSpeed = add(new NumberSetting("Animation speed", 1.0, 0.3, 3.0, 0.1));

        ThemeModule() {
            super("Theme", "Client appearance", Category.THEMES);
            restoreEnabled(true);
            theme.onChange = n -> { apply(n); ConfigManager.save(); };
        }
    }

    public static ThemeModule THEME;

    // from == to  -> idle. Otherwise we are fading from -> to, started at `start`.
    private static Theme from = Themes.DEFAULT, to = Themes.DEFAULT, shown = Themes.DEFAULT;
    private static long start, stampMs = -1;

    public static void init() { THEME = new ThemeModule(); snap(THEME.theme.get()); }

    /** Live switch with a 250 ms fade (restarts from the colours currently on screen). */
    public static void apply(String name) {
        Theme n = Themes.byName(name);
        if (n == null || n == to) return;
        from = current();
        to = n;
        start = System.nanoTime();
        stampMs = -1;
    }

    /** Switch without animation. */
    public static void snap(String name) {
        Theme n = Themes.byName(name);
        if (n == null) return;
        from = to = shown = n;
    }

    /** Blended palette for this instant; cheap enough to call many times per frame. */
    public static Theme current() {
        if (from == to) return to;
        long now = System.nanoTime();
        float t = (now - start) / (TRANSITION_MS * 1_000_000f);
        if (t >= 1f) { from = to; shown = to; return to; }
        long ms = now / 1_000_000L;
        if (ms != stampMs) {
            float e = 1f - (1f - t) * (1f - t) * (1f - t);   // ease-out cubic
            shown = Theme.lerp(from, to, e);
            stampMs = ms;
        }
        return shown;
    }

    public static String selectedName() { return to.name(); }

    public static int bg()            { return current().background(); }
    public static int panel()         { return current().panel(); }
    public static int card()          { return current().card(); }
    public static int border()        { return current().border(); }
    public static int text()          { return current().text(); }
    public static int textSecondary() { return current().textSecondary(); }
    public static int accent()        { return current().accent(); }
    public static int hover()         { return current().hover(); }
    public static int active()        { return current().active(); }
    public static int onAccent()      { return current().onAccent(); }

    public static float panelAlpha() { return THEME == null ? 0.92f : THEME.panelAlpha.f(); }
    public static int rounding() { return THEME == null ? 8 : THEME.rounding.i(); }
    public static float animSpeed() { return THEME == null ? 1f : THEME.animSpeed.f(); }
    public static boolean smooth() { return THEME == null || THEME.smooth.get(); }
}
