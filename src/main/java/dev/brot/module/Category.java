package dev.brot.module;

public enum Category {
    CRYSTAL("Crystal", "crystal", false),
    SWORD("Sword", "sword", false),
    MACE("Mace", "mace", false),
    UHC("UHC", "uhc", false),
    MOVEMENT("Movement", "movement", false),
    THEMES("Themes", "themes", true),
    HUD("HUD", "hud", true);

    public final String label, icon;
    public final boolean misc;
    Category(String label, String icon, boolean misc) { this.label = label; this.icon = icon; this.misc = misc; }
}
