package dev.brot.theme;

import java.util.List;

/** The 15 built-in themes (name, background, accent). */
public final class Themes {
    private Themes() {}

    public static final List<Theme> LIST = List.of(
        Theme.of("Void",       0x08090C, 0x7777FF),
        Theme.of("Eclipse",    0x101014, 0xC0A0FF),
        Theme.of("Nord",       0x111820, 0x88AFCB),
        Theme.of("Glacier",    0x10191D, 0x8DE1E8),
        Theme.of("Burgundy",   0x190F13, 0xB96878),
        Theme.of("Forest",     0x101712, 0x83B99A),
        Theme.of("Titanium",   0x17191D, 0xB5BEC9),
        Theme.of("Pure",       0x101010, 0xFFFFFF),
        Theme.of("Mocha",      0x191512, 0xC49A78),
        Theme.of("Lavender",   0x171321, 0xB9A1E8),
        Theme.of("Deep Ocean", 0x0C151D, 0x579BC5),
        Theme.of("Scarlet",    0x180F12, 0xE66B73),
        Theme.of("Platinum",   0xE9E9EC, 0x5555AA),
        Theme.of("Amber",      0x19150E, 0xD9AD55),
        Theme.of("Stealth",    0x090A0B, 0x73777D));

    public static final Theme DEFAULT = LIST.get(1); // Eclipse

    public static Theme byName(String n) {
        if (n == null) return null;
        for (Theme t : LIST) if (t.name().equalsIgnoreCase(n)) return t;
        return null;
    }
}
