package dev.brot.module.crystal;

import dev.brot.module.InventoryManager;
import dev.brot.setting.*;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

/**
 * While right mouse is held, the Crystal Key slot is selected with an end crystal in hand and the crosshair is on an
 * end crystal: attack it at a CPS drawn from [CPS Min, CPS Max]; Jitter randomises each interval by +-Jitter %.
 */
public final class AutoHitCrystal extends CrystalModule {
    private final KeySetting crystal = hotbarKey("Crystal Key", 1);
    private final NumberSetting jitter = add(new NumberSetting("Jitter", 10, 0, 50, 1).unit("%"));
    private final NumberSetting cpsMin = add(new NumberSetting("CPS Min", 8, 1, 20, 0.5));
    private final NumberSetting cpsMax = add(new NumberSetting("CPS Max", 12, 1, 20, 0.5));
    private long nextAt;

    public AutoHitCrystal() {
        super("Auto Hit Crystal", "Hits the crystal you aim at while right mouse is held");
        // range validation: the two sliders can never cross, also when loaded from config
        cpsMin.onChange = v -> { if (v > cpsMax.get()) cpsMax.setClamped(v); };
        cpsMax.onChange = v -> { if (v < cpsMin.get()) cpsMin.setClamped(v); };
    }

    @Override protected void reset() { nextAt = 0; }

    @Override protected void update() {
        if (!noScreen() || !mc.options.useKey.isPressed()) return;
        if (InventoryManager.selected() != crystal.slot() || !mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hit) || !(hit.getEntity() instanceof EndCrystalEntity target)) return;
        long now = System.nanoTime() / 1_000_000L;
        if (now < nextAt) return;
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        nextAt = now + interval();
    }

    private long interval() {
        var r = ThreadLocalRandom.current();
        double lo = Math.min(cpsMin.get(), cpsMax.get()), hi = Math.max(cpsMin.get(), cpsMax.get());
        double cps = lo + r.nextDouble() * (hi - lo);
        double j = jitter.get() / 100.0;
        return Math.round(1000.0 / cps * (1.0 + j * (r.nextDouble() * 2 - 1)));
    }
}
