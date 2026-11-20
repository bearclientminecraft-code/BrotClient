package dev.brot.hud;

import dev.brot.render.*;
import dev.brot.theme.ThemeManager;
import net.minecraft.client.gui.DrawContext;

public class WatermarkElement extends HudElement {
    public WatermarkElement() { super("Watermark", 6, 6); }
    @Override public int width() { return 70; }
    @Override public int height() { return 16; }
    @Override public void draw(DrawContext c) {
        GUIRenderer.roundBorder(c, 0, 0, width(), height(), ThemeManager.rounding() / 2 + 2,
            GUIRenderer.alpha(ThemeManager.panel(), 0.9f), ThemeManager.border());
        FontRenderer.drawCentered(c, "BROT", width() / 2, 4, ThemeManager.accent());
    }
}
