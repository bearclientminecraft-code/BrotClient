package dev.brot.module.movement;

import dev.brot.module.*;
import dev.brot.setting.NumberSetting;

/**
 * Caps LivingEntity#jumpingCooldown (vanilla: 10 ticks between held-jump hops). Needs the access-widener entry
 * "accessible field LivingEntity jumpingCooldown I" (added to brot.accesswidener). Only the cooldown is touched, the
 * jump itself, ground check and everything else stay vanilla.
 */
public final class NoJumpDelay extends TickModule {
    private final NumberSetting delay = add(new NumberSetting("Jump Delay", 0, 0, 10, 1).unit("ticks"),
        "Ticks between jumps while holding the jump key. Vanilla is 10, 0 removes the delay");

    public NoJumpDelay() { super("No Jump Delay", "Removes or shortens the delay between held jumps", Category.MOVEMENT); }

    @Override protected void update() {
        if (mc.player.jumpingCooldown > delay.i()) mc.player.jumpingCooldown = delay.i();
    }
}
