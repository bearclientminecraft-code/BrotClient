package dev.brot.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/**
 * Custom GUI pipeline with an SDF fragment shader (assets/brot/shaders/core/round.vsh|fsh).
 * Vertex format = vanilla ENTITY-style format; the extra attributes carry the shape data:
 *   UV0 = local position (px, from rect centre), UV1 = half size * 16, UV2.x = radius * 16,
 *   Normal = (border/8, soft/32, mode/2).
 */
public final class ShapePipelines {
    private ShapePipelines() {}
    public static RenderPipeline ROUND;
    public static boolean ready;

    public static void init() {
        try {
            ROUND = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
                .withLocation(Identifier.of("brot", "pipeline/round"))
                .withVertexShader(Identifier.of("brot", "core/round"))
                .withFragmentShader(Identifier.of("brot", "core/round"))
                .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
                .build());
            ready = true;
        } catch (Throwable t) {
            System.err.println("[BROT] shape pipeline failed, using strip fallback: " + t);
            ready = false;
        }
    }
}
