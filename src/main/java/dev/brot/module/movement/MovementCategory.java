package dev.brot.module.movement;

import dev.brot.module.ModuleManager;

/** Registers the Movement modules; the ClickGUI picks them up from ModuleManager automatically. */
public final class MovementCategory {
    private MovementCategory() {}
    public static void register() { ModuleManager.register(new AutoSprint(), new NoJumpDelay()); }
}
