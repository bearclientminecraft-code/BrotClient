package dev.brot.module.crystal;

import dev.brot.module.*;
import dev.brot.module.Module;
import dev.brot.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/** Common base of every Crystal-category module: tick plumbing, cancellation on disable, shared helpers. */
public abstract class CrystalModule extends Module {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    protected final ActionQueue queue = new ActionQueue();

    protected CrystalModule(String name, String description) { super(name, description, Category.CRYSTAL); }

    // ----- setting factories (one place for ranges / units) -----
    protected KeySetting key(String n) { return add(KeySetting.key(n)); }
    protected KeySetting hotbarKey(String n, int slot) { return add(KeySetting.hotbar(n, slot)); }
    protected NumberSetting delayMs(String n, int def, int max) { return add(new NumberSetting(n, def, 0, max, 5).unit("ms")); }
    protected NumberSetting slot(String n, int def) { return add(new NumberSetting(n, def, 1, 9, 1)); }
    protected BooleanSetting check(String n, boolean def) { return add(new BooleanSetting(n, def).asCheckbox()); }
    protected static int idx(NumberSetting slot) { return slot.i() - 1; }

    /** Runs every tick while enabled and a world is loaded. Queue new sequences here. */
    protected abstract void update();
    /** Drop module state (called on disable and when the world goes away). */
    protected void reset() {}

    @Override public final void onTick() {
        if (!isEnabled()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null || !mc.player.isAlive()) {
            queue.clear(); reset(); return;
        }
        try { update(); queue.tick(); }
        catch (RuntimeException ex) { System.err.println("[BROT] " + name + " failed: " + ex); queue.clear(); reset(); }
    }
    @Override protected void onDisable() { queue.clear(); reset(); }   // cancels pending sequences

    // ----- shared helpers -----
    protected boolean noScreen() { return mc.currentScreen == null; }

    protected BlockHitResult blockTarget() {
        return mc.crosshairTarget instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK ? b : null;
    }
    /** Right-click the block under the crosshair with the main hand. */
    protected boolean useBlock() { return useBlock(blockTarget()); }
    protected boolean useBlock(BlockHitResult hit) {
        if (hit == null) return false;
        swingIfClient(mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit), Hand.MAIN_HAND);
        return true;
    }

    // ----- camera (same path as the mouse: Entity#changeLookDirection, units = mouse pixels * sensitivity factor) -----
    /** Pitch change in degrees (positive = look down), applied exactly like mouse movement. */
    protected void rotatePitch(float degrees) { mc.player.changeLookDirection(0.0, degrees / 0.15); }
    /** Mouse pixels -> degrees at the player's current sensitivity (vanilla: px * 8 * (s*0.6+0.2)^3 * 0.15). */
    protected float pixelsToDegrees(double px) {
        double s = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        return (float) (px * 8.0 * s * s * s * 0.15);
    }
    // ----- frame-based pitch animation: advanced from onFrame(), so it is smooth at any FPS and independent of ticks -----
    private boolean animating;
    private float animFrom, animTo;
    private long animStart, animDur;

    /** Move the pitch to `to` over durMs (0 = instantly). Smoothstep easing, runs every rendered frame. */
    protected void startPitchAnim(float to, long durMs) {
        to = Math.max(-90f, Math.min(90f, to));
        if (durMs <= 0) { animating = false; rotatePitch(to - mc.player.getPitch()); return; }
        animFrom = mc.player.getPitch(); animTo = to;
        animStart = System.nanoTime() / 1_000_000L; animDur = durMs; animating = true;
    }
    protected void stopPitchAnim() { animating = false; }

    @Override public final void onFrame() {
        if (!animating) return;
        if (mc.player == null) { animating = false; return; }
        float t = Math.min(1f, (System.nanoTime() / 1_000_000L - animStart) / (float) animDur);
        float e = t * t * (3f - 2f * t);
        rotatePitch(animFrom + (animTo - animFrom) * e - mc.player.getPitch());
        if (t >= 1f) animating = false;
    }
    protected void useItem(Hand hand) { swingIfClient(mc.interactionManager.interactItem(mc.player, hand), hand); }
    /** Mirrors MinecraftClient#doItemUse (1.21.2+ ActionResult.Success / SwingSource) - verify names on 1.21.11. */
    protected void swingIfClient(ActionResult r, Hand hand) {
        if (r instanceof ActionResult.Success s && s.swingSource() == ActionResult.SwingSource.CLIENT) mc.player.swingHand(hand);
    }
    /** Select the hotbar slot of `k`, but only if it really holds `item` (otherwise abort the sequence). */
    protected boolean selectHolding(KeySetting k, Item item) {
        int s = k.slot();
        if (!InventoryManager.hotbarHas(s, item)) s = InventoryManager.findHotbar(item);   // configured slot empty -> any hotbar slot with it
        return s >= 0 && InventoryManager.select(s);
    }
}
