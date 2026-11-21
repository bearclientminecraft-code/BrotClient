package dev.brot.hud;

import dev.brot.module.*;
import dev.brot.module.Module;
import dev.brot.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;

public abstract class HudElement extends Module {
    public int x, y;
    public final NumberSetting scale = add(new NumberSetting("Scale", 1.0, 0.5, 2.5, 0.05));
    public final NumberSetting opacity = add(new NumberSetting("Opacity", 1.0, 0.1, 1.0, 0.05));

    protected HudElement(String name, int x, int y) {
        super(name, "HUD element", Category.HUD);
        this.x = x; this.y = y;
        restoreEnabled(true);
    }
    /** Drawn on screen? (default: the module is enabled; the watermark follows the HUD module's toggle instead). */
    public boolean shown() { return isEnabled(); }
    /** Called before each render; elements with a computed default position (x < 0) resolve it here. */
    public void layoutDefault(int screenW) {}
    public abstract int width();
    public abstract int height();
    /** Draw at (0,0); HUDManager applies position and scale. */
    public abstract void draw(DrawContext ctx);
}
