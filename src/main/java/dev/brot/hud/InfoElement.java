package dev.brot.hud;

import dev.brot.render.*;
import dev.brot.theme.ThemeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class InfoElement extends HudElement {
    public InfoElement() { super("Info", 6, 28); }
    @Override public int width() { return 90; }
    @Override public int height() { return 28; }
    @Override public void draw(DrawContext c) {
        var mc = MinecraftClient.getInstance();
        GUIRenderer.roundBorder(c, 0, 0, width(), height(), 5, GUIRenderer.alpha(ThemeManager.panel(), 0.9f), ThemeManager.border());
        FontRenderer.draw(c, "FPS: " + mc.getCurrentFps(), 5, 4, ThemeManager.text());
        String pos = mc.player == null ? "-" : mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ();
        FontRenderer.draw(c, pos, 5, 16, ThemeManager.textSecondary());
    }
}
