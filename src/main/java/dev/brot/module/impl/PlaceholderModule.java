package dev.brot.module.impl;

import dev.brot.module.*;
import dev.brot.module.Module;
import dev.brot.setting.*;

/** GUI-only demo module so every setting type is visible. No gameplay behaviour. */
public class PlaceholderModule extends Module {
    public PlaceholderModule(String name, Category c) {
        super(name, "Placeholder module", c);
        add(new NumberSetting("Value", 3.0, 0, 6, 0.1));
        add(new BooleanSetting("Option", true));
        add(new ModeSetting("Mode", "A", "A", "B", "C"));
    }
}
