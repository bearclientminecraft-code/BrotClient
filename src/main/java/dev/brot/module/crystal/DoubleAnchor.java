package dev.brot.module.crystal;

import dev.brot.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Anchor 1: place -> (First Delay) charge -> (Second Delay) detonate.
 * (Switch Delay) Anchor 2 on the same spot: place -> (First Delay) charge -> (Second Delay) detonate.
 * The second anchor is placed against the SAME supporting block face as the first (never "in the air").
 * If you start while already aiming at an anchor, the block under it is used as the support (must not be air);
 * that fallback is untested.
 */
public final class DoubleAnchor extends AnchorModule {
    private final NumberSetting first = delayMs("First Delay", 50, 500), second = delayMs("Second Delay", 50, 500),
        switchDelay = delayMs("Switch Delay", 50, 500);

    public DoubleAnchor() { super("Double Anchor", "Place, charge and detonate two anchors on the same spot in a row"); }

    @Override protected void update() {
        if (!triggered()) return;
        BlockHitResult t = blockTarget();
        if (t == null) return;
        boolean aimed = isAnchor(t);
        BlockHitResult support = t;
        if (aimed) {
            BlockPos below = t.getBlockPos().down();
            if (mc.world.getBlockState(below).isAir()) return;
            support = new BlockHitResult(Vec3d.ofCenter(below).add(0, 0.5, 0), Direction.UP, below, false);
        }
        final BlockHitResult placeHit = support;
        if (!aimed) queue.add(0, this::placeAnchor);
        queue.add(aimed ? 0 : first.i(), this::charge);
        queue.add(second.i(), this::detonate);
        queue.add(switchDelay.i(), () -> selectHolding(anchor, Items.RESPAWN_ANCHOR) && useBlock(placeHit));
        queue.add(first.i(), this::charge);
        queue.add(second.i(), this::detonate);
    }
}
