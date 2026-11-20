package dev.brot.module.crystal;

import dev.brot.module.InventoryManager;
import dev.brot.setting.*;
import net.minecraft.entity.decoration.EndCrystalEntity;

/**
 * Threat -> switch to Totem Slot (after Reaction Delay) -> optionally back to Return Slot (after Return Delay).
 * Only reacts while Main Slot is selected. Threat = launched upwards (Launch) and/or an end crystal within
 * Crystal Range (Crystal). One trigger per threat: re-arms after the threat has gone.
 * Constants not in the spec: launch = upward velocity above LAUNCH_VY while airborne (untested threshold).
 */
public final class AutoDoubleHand extends CrystalModule {
    private static final double LAUNCH_VY = 0.6;
    private final NumberSetting totemSlot = slot("Totem Slot", 2), mainSlot = slot("Main Slot", 1), returnSlot = slot("Return Slot", 1);
    private final ModeSetting mode = add(new ModeSetting("Detection Mode", "Both", "Launch", "Crystal", "Both"));
    private final NumberSetting range = add(new NumberSetting("Crystal Range", 6, 3, 12, 0.5));
    private final NumberSetting reaction = delayMs("Reaction Delay", 20, 300);
    private final BooleanSetting autoReturn = check("Auto Return", true);
    private final NumberSetting returnDelay = delayMs("Return Delay", 300, 1000);
    private boolean latched;

    public AutoDoubleHand() {
        super("Auto Double Hand", "Switches to a totem slot when you are launched or a crystal is near");
        range.visibleWhen(() -> !mode.is("Launch"));
        returnDelay.visibleWhen(autoReturn::get);
        returnSlot.visibleWhen(autoReturn::get);
    }

    @Override protected void reset() { latched = false; }

    private boolean threat() {
        var p = mc.player;
        boolean launch = !mode.is("Crystal") && !p.isOnGround() && p.getVelocity().y > LAUNCH_VY;
        boolean crystal = !mode.is("Launch")
            && !mc.world.getEntitiesByClass(EndCrystalEntity.class, p.getBoundingBox().expand(range.get()), e -> true).isEmpty();
        return launch || crystal;
    }

    @Override protected void update() {
        if (!threat()) { latched = false; return; }
        if (latched || queue.running() || !noScreen()) return;
        if (InventoryManager.selected() != idx(mainSlot)) return;
        if (!InventoryManager.isTotem(InventoryManager.hotbarStack(idx(totemSlot)))) return;
        latched = true;
        queue.add(reaction.i(), () -> InventoryManager.select(idx(totemSlot)));
        if (autoReturn.get()) queue.add(returnDelay.i(), () -> InventoryManager.select(idx(returnSlot)));
    }
}
