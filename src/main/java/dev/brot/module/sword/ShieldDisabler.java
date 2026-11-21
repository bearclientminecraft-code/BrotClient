package dev.brot.module.sword;

import dev.brot.module.*;
import dev.brot.setting.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

/**
 * Target under the crosshair is blocking with a shield -> (select an axe) -> wait HitDelay -> hit.
 * Stun (assumed meaning, see description): after the axe hit, switch back, wait HitDelay and hit once more with the
 * original weapon. Switch Back: return to the original slot SwitchDelay after the hit. RequireHoldAxe: only act while an
 * axe is already in hand (never switches). Random Delay is added on top of both delays.
 */
public final class ShieldDisabler extends TickModule {
    private final NumberSetting hitDelay = add(new NumberSetting("HitDelay", 50, 0, 1000, 5).unit("ms"),
        "Wait between selecting the axe and hitting the shield");
    private final NumberSetting switchDelay = add(new NumberSetting("SwitchDelay", 50, 0, 1000, 5).unit("ms"),
        "Wait between the axe hit and switching back to your previous slot");
    private final BooleanSetting switchBack = add(new BooleanSetting("Switch Back", true),
        "Return to the slot you were on before the axe was selected");
    private final BooleanSetting stun = add(new BooleanSetting("Stun", false),
        "After the shield is hit, switch back and land one follow-up hit with your previous weapon (implies Switch Back)");
    private final BooleanSetting requireAxe = add(new BooleanSetting("RequireHoldAxe", false),
        "Only act while you already hold an axe; never switches items");
    private final RandomDelay random = new RandomDelay(this);

    private int restoreSlot = -1;   // slot to go back to if a sequence was aborted after the axe was selected

    public ShieldDisabler() {
        super("Shield Disabler", "Hits a blocking target with an axe to break its shield", Category.SWORD);
        switchBack.visibleWhen(() -> !requireAxe.get() && !stun.get());
        stun.visibleWhen(() -> !requireAxe.get());
        switchDelay.visibleWhen(() -> !requireAxe.get() && (switchBack.get() || stun.get()));
    }

    @Override protected void reset() { restoreSlot = -1; }

    private static boolean isAxe(ItemStack s) { return s.isIn(ItemTags.AXES); }
    private boolean restores() { return !requireAxe.get() && (switchBack.get() || stun.get()); }

    @Override protected void update() {
        if (!queue.running() && restoreSlot >= 0) {          // a sequence ended early (target lost / shield dropped)
            if (restores()) InventoryManager.select(restoreSlot);
            restoreSlot = -1;
        }
        if (!noScreen() || queue.running()) return;
        LivingEntity t = crosshairLiving();
        if (t == null || !t.isBlocking()) return;

        int origin = InventoryManager.selected();
        boolean holding = isAxe(InventoryManager.hotbarStack(origin));
        if (requireAxe.get() && !holding) return;
        int axe = holding ? origin : InventoryManager.findHotbarMatching(ShieldDisabler::isAxe);
        if (axe < 0) return;
        boolean swap = axe != origin;

        long hit = random.apply(hitDelay.i());
        long back = random.apply(switchDelay.i());
        if (swap) { restoreSlot = origin; queue.add(0, () -> InventoryManager.select(axe)); }
        queue.add(hit, () -> {                                  // re-validate: target may have moved or dropped the shield
            LivingEntity c = crosshairLiving();
            if (c == null || !c.isBlocking()) return false;
            attack(c);
            return true;
        });
        if (swap && restores()) {
            queue.add(back, () -> { restoreSlot = -1; return InventoryManager.select(origin); });
            if (stun.get()) queue.add(hit, () -> {
                LivingEntity c = crosshairLiving();
                if (c == null) return false;
                attack(c);
                return true;
            });
        }
    }
}
