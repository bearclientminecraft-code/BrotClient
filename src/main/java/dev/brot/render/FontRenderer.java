package dev.brot.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Draws text with Inter (assets/brot/font/inter.json -> inter.ttf) through the vanilla TextRenderer.
 * If the font does not load, text silently falls back to the default font.
 * NOTE: Style.withFont(StyleSpriteSource) is the 1.21.9+ form (verify); older versions: Style.withFont(Identifier).
 */
public final class FontRenderer {
    private FontRenderer() {}
    public static final Identifier INTER = Identifier.of("brot", "inter");
    private static final Style STYLE = Style.EMPTY.withFont(new StyleSpriteSource.Font(INTER));

    private static TextRenderer tr() { return MinecraftClient.getInstance().textRenderer; }
    private static Text t(String s) { return Text.literal(s).setStyle(STYLE); }

    public static void draw(DrawContext c, String s, int x, int y, int color) { c.drawText(tr(), t(s), x, y, GUIRenderer.a(color), false); }
    public static void drawCentered(DrawContext c, String s, int cx, int y, int color) { draw(c, s, cx - width(s) / 2, y, color); }
    public static int width(String s) { return tr().getWidth(t(s)); }
    public static int height() { return tr().fontHeight; }
}
