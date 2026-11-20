package dev.brot.render;

/**
 * IMPLEMENTED: vanilla menu blur, produced by Screen#render -> renderBackground (called first by ClickGUI).
 * NOT IMPLEMENTED: own Gaussian blur (Framebuffer + custom shader). Since 1.21.6 rendering goes through
 * RenderPipeline/RenderPass; a custom post pass must be written against the real 1.21.11 sources.
 */
public final class BlurRenderer {
    private BlurRenderer() {}
    public static final int EXTRA_DIM = 0x40000000;
}
