package dev.brot.module.sword;

import dev.brot.module.ModuleManager;

/** Registers the Sword modules; the ClickGUI picks them up from ModuleManager automatically. */
public final class SwordCategory {
    private SwordCategory() {}
    public static void register() { ModuleManager.register(new ShieldDisabler(), new TriggerBot()); }
}
