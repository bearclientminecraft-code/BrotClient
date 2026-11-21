package dev.brot.module.sword;

import dev.brot.gui.ClickGUI;
import dev.brot.module.*;
import dev.brot.setting.*;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import org.lwjgl.glfw.GLFW;

/**
 * Attacks the living entity under the crosshair when every condition holds. Evaluated every rendered frame (the delays
 * are real milliseconds); the game's attack-cooldown meter itself only advances per tick. Per attack: conditions ok -> roll Hit Chance -> wait (weapon delay + Random Delay)
 * -> re-check -> hit. Losing the target, leaving reach, switching items or recharging cancels the pending hit.
 */
public final class TriggerBot extends TickModule {
    private static final long FAIL_HOLDOFF_MS = 350;   // after a failed Hit Chance roll, don't re-roll for this long

    private final BooleanSetting workInScreen = add(new BooleanSetting("Work In Screen", false),
        "Keep working while a screen (inventory, chat...) is open. Never while this menu is open");
    private final BooleanSetting whileUse = add(new BooleanSetting("While Use", false),
        "Allow attacks while using an item (eating, blocking, drawing a bow)");
    private final BooleanSetting onLeftClick = add(new BooleanSetting("On Left Click", false),
        "Only attack while the left mouse button is held");
    private final BooleanSetting allItems = add(new BooleanSetting("All Items", false),
        "Attack with any held item. Off: only swords and axes");
    private final BooleanSetting weaponDetector = add(new BooleanSetting("Weapon Detector", true),
        "Detect the held weapon: axes use the Axe Delay, everything else the Sword Delay. Off: Sword Delay always");
    private final BooleanSetting checkShield = add(new BooleanSetting("Check Shield", true),
        "Do not hit a blocking target, unless you hold an axe (which breaks the shield)");
    private final BooleanSetting earlyClick = add(new BooleanSetting("Early Click", false),
        "Attack before the attack cooldown is full (less damage, faster hits)");
    private final NumberSetting earlyAt = add(new NumberSetting("Early Click At", 90, 50, 99, 1).unit("%"),
        "Attack cooldown charge at which the early click is allowed");

    private final MinMax reach = new MinMax(this, "Reach Min", "Reach Max", 2.8, 3.0, 0, 6, 0.05, "blocks",
        "Lower bound of the random reach limit", "Upper bound of the random reach limit (the game's own range still caps it)");
    private final MinMax swordDelay = new MinMax(this, "Sword Delay Min", "Sword Delay Max", 40, 90, 0, 1000, 5, "ms",
        "Shortest wait before a sword/other hit", "Longest wait before a sword/other hit");
    private final MinMax axeDelay = new MinMax(this, "Axe Delay Min", "Axe Delay Max", 60, 120, 0, 1000, 5, "ms",
        "Shortest wait before an axe hit", "Longest wait before an axe hit");
    private final RandomDelay random = new RandomDelay(this);
    private final NumberSetting hitChance = add(new NumberSetting("Hit Chance", 100, 0, 100, 1).unit("%"),
        "Chance that a ready hit is actually made");

    private int targetId = -1;
    private double reachLimit;
    private long readyAt = -1, holdoffUntil, lastFrameRun;

    public TriggerBot() {
        super("Trigger Bot", "Attacks the target under your crosshair", Category.SWORD);
        earlyAt.visibleWhen(earlyClick::get);
        axeDelay.visibleWhen(weaponDetector::get);
    }

    @Override protected void reset() { idle(); holdoffUntil = 0; }
    private void idle() { targetId = -1; readyAt = -1; }

    private boolean canRun() {
        if (mc.currentScreen != null && (!workInScreen.get() || mc.currentScreen instanceof ClickGUI)) return false;
        if (!whileUse.get() && mc.player.isUsingItem()) return false;
        return !onLeftClick.get() || mc.options.attackKey.isPressed()
            || KeybindManager.down(KeybindManager.mouse(GLFW.GLFW_MOUSE_BUTTON_LEFT));
    }

    /** Per-tick fallback: only runs when no frame did for 100 ms (HUD hidden with F1, frozen render). */
    @Override protected void update() { if (nowMs() - lastFrameRun > 100) run(); }

    /** Main driver: every rendered frame, so timing follows FPS instead of the 20 Hz tick. */
    @Override protected void frameUpdate() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || !mc.player.isAlive()) { idle(); return; }
        lastFrameRun = nowMs();
        try { run(); } catch (RuntimeException ex) { System.err.println("[BROT] " + name + " failed: " + ex); idle(); }
    }

    private void run() {
        if (!canRun()) { idle(); return; }
        LivingEntity t = crosshairLiving();
        if (t == null) { idle(); return; }

        if (t.getId() != targetId) { targetId = t.getId(); readyAt = -1; reachLimit = reach.sample(); }
        reachLimit = Math.max(reach.min.get(), Math.min(reach.max.get(), reachLimit));   // follow live setting changes
        if (mc.player.getEyePos().distanceTo(mc.crosshairTarget.getPos()) > reachLimit) { readyAt = -1; return; }

        ItemStack held = mc.player.getMainHandStack();
        boolean axe = held.isIn(ItemTags.AXES);
        if (!allItems.get() && !axe && !held.isIn(ItemTags.SWORDS)) { readyAt = -1; return; }
        if (checkShield.get() && t.isBlocking() && !axe) { readyAt = -1; return; }

        float need = earlyClick.get() ? earlyAt.f() / 100f : 1f;
        if (mc.player.getAttackCooldownProgress(0.5f) < need) { readyAt = -1; return; }

        long now = nowMs();
        if (readyAt < 0) {
            if (now < holdoffUntil) return;
            if (ThreadLocalRandom.current().nextInt(100) >= hitChance.i()) { holdoffUntil = now + FAIL_HOLDOFF_MS; return; }
            MinMax d = weaponDetector.get() && axe ? axeDelay : swordDelay;
            readyAt = now + random.apply(d.sampleMs());
        }
        if (now < readyAt) return;
        readyAt = -1;
        attack(t);
    }
}
