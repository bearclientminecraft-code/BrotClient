package dev.brot.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;

/** One anti-aliased rounded shape (fill, outline or shadow). Component names match SimpleGuiElementRenderState. */
public record ShapeState(RenderPipeline pipeline, TextureSetup textureSetup, ScreenRect scissorArea, ScreenRect bounds,
                         Matrix3x2f pose, float x, float y, float w, float h, float radius, float pad,
                         int mode, float border, float soft, int cTL, int cBL, int cBR, int cTR)
        implements SimpleGuiElementRenderState {

    @Override
    public void setupVertices(VertexConsumer v) {
        float hw = w / 2f, hh = h / 2f;
        float x1 = x - pad, y1 = y - pad, x2 = x + w + pad, y2 = y + h + pad;
        float l1 = -hw - pad, t1 = -hh - pad, l2 = hw + pad, t2 = hh + pad;
        vert(v, x1, y1, l1, t1, cTL);
        vert(v, x1, y2, l1, t2, cBL);
        vert(v, x2, y2, l2, t2, cBR);
        vert(v, x2, y1, l2, t1, cTR);
    }

    private void vert(VertexConsumer v, float px, float py, float lu, float lv, int color) {
        v.vertex(pose, px, py)
         .color(color)
         .texture(lu, lv)
         .overlay(Math.round(w * 8f), Math.round(h * 8f))        // half size * 16
         .light(Math.round(radius * 16f), 0)
         .normal(Math.min(1f, border / 8f), Math.min(1f, soft / 32f), mode / 2f);
    }
}
