package dev.brot.module.movement;

import dev.brot.module.*;
import net.minecraft.client.option.KeyBinding;

/**
 * Holds the vanilla sprint key while forward is held, so the game's own sprint rules (hunger, blindness, collisions,
 * sneaking, using items) stay in charge - nothing sets the sprint flag directly. Releases it again when you stop.
 * Note: with the "Toggle Sprint" video option the key cannot be released from here (vanilla sticky key behaviour).
 */
public final class AutoSprint extends TickModule {
    private boolean forced;

    public AutoSprint() { super("Auto Sprint", "Sprints automatically while you move forward", Category.MOVEMENT); }

    @Override protected void update() {
        KeyBinding sprint = mc.options.sprintKey;
        boolean want = noScreen() && mc.options.forwardKey.isPressed() && !mc.options.sneakKey.isPressed();
        if (want) {
            if (!sprint.isPressed()) sprint.setPressed(true);   // guard: a toggle-style key would flip every call
            forced = true;
        } else if (forced) release();
    }

    private void release() { mc.options.sprintKey.setPressed(false); forced = false; }
    @Override protected void reset() { if (forced) release(); }
}
