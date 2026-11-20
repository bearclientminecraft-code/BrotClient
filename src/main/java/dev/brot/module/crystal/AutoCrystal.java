package dev.brot.module.crystal;

import dev.brot.module.KeybindManager;
import dev.brot.setting.*;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Activate Key: place obsidian on the block under the crosshair (skipped if you already aim at obsidian/bedrock),
 * then place an end crystal on it. The Crystal slot stays selected afterwards.
 */
public final class AutoCrystal extends CrystalModule {
    private final KeySetting activate = key("Activate Key");
    private final KeySetting obsidian = hotbarKey("Obsidian Key", 1), crystal = hotbarKey("Crystal Key", 2);
    private final NumberSetting delay = delayMs("Delay", 50, 500);

    public AutoCrystal() { super("Auto Crystal", "Places obsidian, then an end crystal on it"); }

    private boolean isBase(BlockHitResult h) {
        if (h == null) return false;
        var b = mc.world.getBlockState(h.getBlockPos());
        return b.isOf(Blocks.OBSIDIAN) || b.isOf(Blocks.BEDROCK);
    }

    @Override protected void update() {
        if (!noScreen() || queue.running() || !KeybindManager.pressed(activate)) return;
        BlockHitResult t = blockTarget();
        if (t == null) return;
        boolean onBase = isBase(t);
        if (!onBase) queue.add(0, () -> selectHolding(obsidian, Items.OBSIDIAN) && useBlock());
        queue.add(onBase ? 0 : delay.i(), () -> isBase(blockTarget()) && selectHolding(crystal, Items.END_CRYSTAL) && useBlock());
    }
}
