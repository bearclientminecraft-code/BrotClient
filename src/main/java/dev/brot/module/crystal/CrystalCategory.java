package dev.brot.module.crystal;

import dev.brot.module.ModuleManager;

/** Registers the Crystal modules; the ClickGUI picks them up from ModuleManager automatically. */
public final class CrystalCategory {
    private CrystalCategory() {}
    public static void register() {
        ModuleManager.register(new AnchorMacro(), new SafeAnchor(), new DoubleAnchor(), new AnchorDash(),
            new AutoHitCrystal(), new AutoDoubleHand(), new HoverTotem(), new InventoryTotem(), new FastXp(),
            new PearlMacro());
    }
}
