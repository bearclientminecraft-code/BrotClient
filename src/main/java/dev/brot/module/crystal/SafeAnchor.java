package dev.brot.module.crystal;

import dev.brot.module.InventoryManager;
import dev.brot.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Place anchor -> (Delay) charge -> (Delay) flick the camera DOWN by Flick Pixels -> right-click the block under the
 * crosshair with glowstone (shield) and select the totem -> (Safety Delay) flick back to the exact original pitch
 * -> detonate. The flick is animated every rendered frame over "Flick Smooth" ms (0 = instant), so it is smooth at any FPS.
 * The sequence always finishes (aim restored, anchor detonated); disabling the module mid-flick restores the pitch.
 * "Placement Mode" is NOT implemented (behaviour not specified).
 */
public final class SafeAnchor extends AnchorModule {
    private final NumberSetting delay = delayMs("Delay", 50, 500), safety = delayMs("Safety Delay", 50, 500);
    private final NumberSetting flickPx = add(new NumberSetting("Flick Pixels", 40, 0, 100, 1).unit("px"));
    private final NumberSetting smooth = add(new NumberSetting("Flick Smooth", 100, 0, 300, 10).unit("ms"));
    private float origPitch;
    private boolean flicking;

    public SafeAnchor() { super("Safe Anchor", "Anchor with a glowstone shield placed after a downward flick"); }

    @Override protected void reset() {
        stopPitchAnim();
        if (flicking && mc.player != null) rotatePitch(origPitch - mc.player.getPitch());
        flicking = false;
    }

    @Override protected void update() {
        if (!triggered()) return;
        BlockHitResult t = blockTarget();
        if (t == null) return;
        boolean aimed = isAnchor(t);
        long d = delay.i(), ms = smooth.i();

        if (!aimed) queue.add(0, this::placeAnchor);
        queue.add(aimed ? 0 : d, this::charge);
        queue.add(d, () -> {                                   // flick down
            origPitch = mc.player.getPitch();
            flicking = true;
            startPitchAnim(origPitch + pixelsToDegrees(flickPx.get()), ms);
            return true;
        });
        queue.add(ms, () -> {                                  // shield: glowstone on whatever block is under the crosshair
            if (selectHolding(glow, Items.GLOWSTONE)) useBlock();
            InventoryManager.select(totem.slot());
            return true;
        });
        queue.add(safety.i(), () -> { startPitchAnim(origPitch, ms); return true; });   // flick back
        queue.add(ms, () -> { flicking = false; return detonate(); });
    }
}
