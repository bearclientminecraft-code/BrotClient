package dev.brot.module.crystal;

import dev.brot.setting.NumberSetting;
import net.minecraft.util.hit.BlockHitResult;

/** Active Key: [place anchor unless already aimed at one] -> charge with glowstone -> detonate with the totem slot. */
public final class AnchorMacro extends AnchorModule {
    private final NumberSetting delay = delayMs("Delay", 50, 500);

    public AnchorMacro() { super("Anchor Macro", "Place, charge and detonate a respawn anchor"); }

    @Override protected void update() {
        if (!triggered()) return;
        BlockHitResult t = blockTarget();
        if (t == null) return;
        boolean aimed = isAnchor(t);
        long d = delay.i();
        if (!aimed) queue.add(0, this::placeAnchor);
        queue.add(aimed ? 0 : d, this::charge);
        queue.add(d, this::detonate);
    }
}
