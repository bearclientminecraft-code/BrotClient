package dev.brot.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Hotbar / inventory helpers. All methods are no-ops (return false) when there is no player. */
public final class InventoryManager {
    private InventoryManager() {}
    private static MinecraftClient mc() { return MinecraftClient.getInstance(); }

    public static int selected() { return mc().player.getInventory().getSelectedSlot(); }   // 1.21.5+ accessor (verify)

    /** Select hotbar slot 0..8. The selected-slot packet is sent by the interaction manager on the next interaction/tick. */
    public static boolean select(int slot) {
        if (mc().player == null || slot < 0 || slot > 8) return false;
        if (selected() != slot) mc().player.getInventory().setSelectedSlot(slot);               // 1.21.5+ accessor (verify)
        return true;
    }
    public static ItemStack hotbarStack(int slot) { return mc().player.getInventory().getStack(slot); }
    public static boolean hotbarHas(int slot, Item item) { return slot >= 0 && slot <= 8 && hotbarStack(slot).isOf(item); }
    /** First hotbar slot (0..8) holding `item`, or -1. */
    public static int findHotbar(Item item) {
        for (int i = 0; i < 9; i++) if (hotbarStack(i).isOf(item)) return i;
        return -1;
    }
    public static boolean isTotem(ItemStack s) { return s.isOf(Items.TOTEM_OF_UNDYING); }

    /** Swap the clicked screen-handler slot with the off hand (vanilla swap button 40). */
    public static boolean swapToOffhand(int handlerSlotId) {
        var mc = mc();
        if (mc.player == null || mc.interactionManager == null) return false;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, handlerSlotId, 40, SlotActionType.SWAP, mc.player);
        return true;
    }

    /** Swap the clicked screen-handler slot with hotbar slot 0..8 (vanilla number-key swap). */
    public static boolean swapToHotbar(int handlerSlotId, int hotbar) {
        var mc = mc();
        if (mc.player == null || mc.interactionManager == null || hotbar < 0 || hotbar > 8) return false;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, handlerSlotId, hotbar, SlotActionType.SWAP, mc.player);
        return true;
    }
}
