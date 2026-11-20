package dev.brot.module.crystal;

import dev.brot.setting.*;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * Throws experience bottles every Delay ms. Works only with a bottle in hand (main hand first, then off hand).
 * Hold Mode on : throws while right mouse is held. Stop When Released on = stops at once; off = one trailing throw.
 * Hold Mode off: a right-click press toggles continuous throwing.
 */
public final class FastXp extends CrystalModule {
    private final NumberSetting delay = delayMs("Delay", 50, 500);
    private final BooleanSetting holdMode = check("Hold Mode", true);
    private final BooleanSetting stopWhenReleased = check("Stop When Released", true);
    private boolean active, trailing, prevUse;
    private long nextAt;

    public FastXp() {
        super("Fast XP", "Rapidly throws experience bottles");
        stopWhenReleased.visibleWhen(holdMode::get);
    }

    @Override protected void reset() { active = trailing = prevUse = false; }

    private Hand xpHand() {
        if (mc.player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE)) return Hand.MAIN_HAND;
        if (mc.player.getOffHandStack().isOf(Items.EXPERIENCE_BOTTLE)) return Hand.OFF_HAND;
        return null;
    }

    @Override protected void update() {
        boolean use = mc.options.useKey.isPressed();
        boolean press = use && !prevUse, release = !use && prevUse;
        prevUse = use;
        Hand hand = xpHand();
        if (hand == null || !noScreen()) { active = trailing = false; return; }

        if (holdMode.get()) {
            if (use) { active = true; trailing = false; }
            else if (release) { active = false; trailing = !stopWhenReleased.get(); }
        } else if (press) active = !active;

        if (!active && !trailing) return;
        long now = System.nanoTime() / 1_000_000L;
        if (now < nextAt) return;
        useItem(hand);
        nextAt = now + delay.i();
        trailing = false;
    }
}
