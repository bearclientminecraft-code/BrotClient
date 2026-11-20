package dev.brot.module.crystal;

import dev.brot.module.InventoryManager;
import dev.brot.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Place anchor -> (Delay) charge -> (Delay) flick the camera DOWN by Flick Pixels (spread over Flick Smooth ticks)
 * -> place a glowstone block on the block under the crosshair as a shield, select the totem
 * -> (Safety Delay) flick back to the exact original pitch -> detonate with the totem.
 * If the shield could not be placed (no block under the crosshair / no glowstone) the aim is restored and the anchor
 * is NOT detonated. Disabling the module mid-flick restores the pitch. "Placement Mode" is NOT implemented
 * (behaviour not specified).
 */
public final class SafeAnchor extends AnchorModule {
    private final NumberSetting delay = delayMs("Delay", 50, 500), safety = delayMs("Safety Delay", 50, 500);
    private final NumberSetting flickPx = add(new NumberSetting("Flick Pixels", 40, 0, 100, 1).unit("px"));
    private final NumberSetting smooth = add(new NumberSetting("Flick Smooth", 3, 1, 10, 1).unit("ticks"));
    private float origPitch;
    private boolean flicking, shielded;

    public SafeAnchor() { super("Safe Anchor", "Anchor with a glowstone shield placed after a downward flick"); }

    @Override protected void reset() {
        if (flicking && mc.player != null) rotatePitch(origPitch - mc.player.getPitch());
        flicking = shielded = false;
    }

    @Override protected void update() {
        if (!triggered()) return;
        BlockHitResult t = blockTarget();
        if (t == null) return;
        boolean aimed = isAnchor(t);
        long d = delay.i();
        int steps = smooth.i();
        shielded = false;

        if (!aimed) queue.add(0, this::placeAnchor);
        queue.add(aimed ? 0 : d, this::charge);
        queue.add(d, () -> { origPitch = mc.player.getPitch(); flicking = true; return true; });
        queuePitch(0, () -> Math.max(-90f, Math.min(90f, origPitch + pixelsToDegrees(flickPx.get()))), steps);
        queue.add(0, () -> {
            BlockHitResult h = blockTarget();
            shielded = h != null && !isAnchor(h) && selectHolding(glow, Items.GLOWSTONE) && useBlock(h);
            InventoryManager.select(totem.slot());
            return true;
        });
        queuePitch(safety.i(), () -> origPitch, steps);
        queue.add(0, () -> { flicking = false; return shielded && detonate(); });
    }
}
