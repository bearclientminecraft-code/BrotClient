package dev.brot.module.crystal;

import dev.brot.module.InventoryManager;
import dev.brot.setting.*;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Hovering an item in your own inventory for Hover Delay moves it (no click needed).
 * Offhand Totem on: the totem goes to the left hand first; once the left hand holds a totem, the next one goes to
 * Target Slot. Offhand Totem off: always Target Slot.
 * Only touches the hovered slot (never armour, never other containers). An occupied destination is left alone unless
 * Auto Replace is on (vanilla swap: the old item lands in the hovered slot - the only case where another item moves).
 * A destination that already holds a totem is never replaced.
 */
public final class HoverTotem extends CrystalModule {
    private final NumberSetting target = slot("Target Slot", 1);
    private final NumberSetting hoverDelay = delayMs("Hover Delay", 100, 1000);
    private final BooleanSetting offhand = check("Offhand Totem", true);
    private final BooleanSetting onlyTotem = check("Only Totem", true);
    private final BooleanSetting autoReplace = check("Auto Replace", false);
    private Slot last;
    private long since;
    private boolean done;

    public HoverTotem() { super("Hover Totem", "Moves a hovered totem to your left hand / target hotbar slot"); }

    @Override protected void reset() { last = null; done = false; }

    @Override protected void update() {
        if (!(mc.currentScreen instanceof HandledScreen<?> screen)) { reset(); return; }
        Slot s = screen.focusedSlot;                                  // widened in brot.accesswidener
        if (s == null || s.inventory != mc.player.getInventory()) { reset(); return; }
        long now = System.nanoTime() / 1_000_000L;
        if (s != last) { last = s; since = now; done = false; }
        if (done || now - since < hoverDelay.i()) return;

        ItemStack st = s.getStack();
        if (st.isEmpty()) return;
        done = true;
        int idx = s.getIndex(), to = idx(target);
        if (idx < 0 || idx > 35 || (onlyTotem.get() && !InventoryManager.isTotem(st))) return;

        ItemStack off = mc.player.getOffHandStack();
        if (offhand.get() && !InventoryManager.isTotem(off) && (off.isEmpty() || autoReplace.get())) {
            InventoryManager.swapToOffhand(s.id);
            return;
        }
        if (idx == to) return;
        ItemStack occupant = InventoryManager.hotbarStack(to);
        if (!occupant.isEmpty() && (InventoryManager.isTotem(occupant) || !autoReplace.get())) return;
        InventoryManager.swapToHotbar(s.id, to);
    }
}
