package dev.brot.module.crystal;

import dev.brot.module.InventoryManager;
import dev.brot.setting.*;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Hovering an item in your own inventory for Hover Delay moves it to Target Slot with a hotbar-swap click
 * (no mouse click needed). Only touches the hovered slot (never armour, never other containers).
 * Target occupied: nothing happens, unless Auto Replace is on - then the vanilla swap puts the old hotbar item
 * into the hovered slot (the only case where another item moves). A hotbar slot already holding a totem is kept.
 */
public final class HoverTotem extends CrystalModule {
    private final NumberSetting target = slot("Target Slot", 1);
    private final NumberSetting hoverDelay = delayMs("Hover Delay", 100, 1000);
    private final BooleanSetting onlyTotem = check("Only Totem", true);
    private final BooleanSetting autoReplace = check("Auto Replace", false);
    private Slot last;
    private long since;
    private boolean done;

    public HoverTotem() { super("Hover Totem", "Moves a hovered totem to your target hotbar slot"); }

    @Override protected void reset() { last = null; done = false; }

    @Override protected void update() {
        if (!(mc.currentScreen instanceof HandledScreen<?> screen)) { reset(); return; }
        Slot s = screen.focusedSlot;                                  // widened in brot.accesswidener
        if (s == null || s.inventory != mc.player.getInventory()) { reset(); return; }
        long now = System.nanoTime() / 1_000_000L;
        if (s != last) { last = s; since = now; done = false; }
        if (done || now - since < hoverDelay.i()) return;

        int idx = s.getIndex(), to = idx(target);
        ItemStack st = s.getStack();
        if (st.isEmpty()) return;
        if (idx < 0 || idx > 35 || idx == to || (onlyTotem.get() && !InventoryManager.isTotem(st))) { done = true; return; }
        ItemStack occupant = InventoryManager.hotbarStack(to);
        done = true;
        if (!occupant.isEmpty() && (InventoryManager.isTotem(occupant) || !autoReplace.get())) return;
        InventoryManager.swapToHotbar(s.id, to);
    }
}
