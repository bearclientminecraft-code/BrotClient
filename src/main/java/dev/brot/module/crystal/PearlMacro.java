package dev.brot.module.crystal;

import dev.brot.module.InventoryManager;
import dev.brot.module.KeybindManager;
import dev.brot.setting.*;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Activate Key: select the pearl slot, right-click (throw), wait Delay, select the totem slot. */
public final class PearlMacro extends CrystalModule {
    private final KeySetting activate = key("Activate Key");
    private final KeySetting pearl = hotbarKey("Pearl Key", 1), totem = hotbarKey("Totem Key", 2);
    private final NumberSetting delay = delayMs("Delay", 50, 500);

    public PearlMacro() { super("Pearl Macro", "Throw an ender pearl and switch back to the totem"); }

    @Override protected void update() {
        if (!noScreen() || queue.running() || !KeybindManager.pressed(activate)) return;
        queue.add(0, () -> { if (!selectHolding(pearl, Items.ENDER_PEARL)) return false; useItem(Hand.MAIN_HAND); return true; });
        queue.add(delay.i(), () -> InventoryManager.select(totem.slot()));
    }
}
