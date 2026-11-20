package dev.brot.module.crystal;

import dev.brot.module.KeybindManager;
import dev.brot.setting.KeySetting;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Shared part of every anchor module: Active Key + the three hotbar keys and the three primitive actions
 * (place / charge / detonate). Each action re-checks the crosshair and that the slot holds the right item,
 * so a sequence stops instead of firing blindly.
 */
abstract class AnchorModule extends CrystalModule {
    protected final KeySetting active = key("Active Key");
    protected final KeySetting anchor = hotbarKey("Anchor Key", 1), glow = hotbarKey("GlowStone Key", 2), totem = hotbarKey("Totem Key", 3);

    protected AnchorModule(String name, String description) { super(name, description); }

    protected boolean triggered() { return noScreen() && !queue.running() && KeybindManager.pressed(active); }
    protected boolean isAnchor(BlockHitResult h) { return h != null && mc.world.getBlockState(h.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR); }

    protected boolean placeAnchor() { return selectHolding(anchor, Items.RESPAWN_ANCHOR) && useBlock(); }
    protected boolean charge()      { return isAnchor(blockTarget()) && selectHolding(glow, Items.GLOWSTONE) && useBlock(); }
    protected boolean detonate()    { return isAnchor(blockTarget()) && selectHolding(totem, Items.TOTEM_OF_UNDYING) && useBlock(); }
}
