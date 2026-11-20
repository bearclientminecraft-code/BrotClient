package dev.brot.module.crystal;

import dev.brot.module.InventoryManager;
import dev.brot.setting.*;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/**
 * Opening the inventory (E, or whatever key opens it) selects Totem Slot after Delay (only if it holds a totem).
 * Closing it selects Previous Slot after Delay when Auto Return is on.
 */
public final class InventoryTotem extends CrystalModule {
    private final NumberSetting totemSlot = slot("Totem Slot", 2), previousSlot = slot("Previous Slot", 1);
    private final BooleanSetting autoReturn = check("Auto Return", true);
    private final NumberSetting delay = delayMs("Delay", 0, 500);
    private boolean wasOpen;

    public InventoryTotem() {
        super("Inventory Totem", "Selects the totem slot when the inventory opens");
        previousSlot.visibleWhen(autoReturn::get);
    }

    @Override protected void onEnable() { wasOpen = mc.currentScreen instanceof InventoryScreen; }
    @Override protected void reset() { wasOpen = false; }

    @Override protected void update() {
        boolean open = mc.currentScreen instanceof InventoryScreen;
        if (open && !wasOpen) {
            queue.clear();
            if (InventoryManager.isTotem(InventoryManager.hotbarStack(idx(totemSlot))))
                queue.add(delay.i(), () -> InventoryManager.select(idx(totemSlot)));
        } else if (!open && wasOpen && autoReturn.get()) {
            queue.clear();
            queue.add(delay.i(), () -> InventoryManager.select(idx(previousSlot)));
        }
        wasOpen = open;
    }
}
