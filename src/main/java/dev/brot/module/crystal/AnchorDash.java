package dev.brot.module.crystal;

import dev.brot.setting.*;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Anchor Macro, then right after the detonation: select Pearl Key slot and right-click (throw the pearl).
 * The pearl is thrown on the very next tick after the explosion step. "Dash Speed" and "Mode" are NOT implemented:
 * their behaviour was not specified.
 */
public final class AnchorDash extends AnchorModule {
    private final KeySetting pearl = hotbarKey("Pearl Key", 4);
    private final NumberSetting delay = delayMs("Delay", 50, 500);

    public AnchorDash() { super("Anchor Dash", "Anchor macro followed by an ender pearl throw"); }

    @Override protected void update() {
        if (!triggered()) return;
        BlockHitResult t = blockTarget();
        if (t == null) return;
        boolean aimed = isAnchor(t);
        long d = delay.i();
        if (!aimed) queue.add(0, this::placeAnchor);
        queue.add(aimed ? 0 : d, this::charge);
        queue.add(d, this::detonate);
        queue.add(0, () -> selectHolding(pearl, Items.ENDER_PEARL) && throwPearl());
    }

    private boolean throwPearl() { useItem(net.minecraft.util.Hand.MAIN_HAND); return true; }
}
