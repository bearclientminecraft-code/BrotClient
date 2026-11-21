package dev.brot.module.crystal;

import dev.brot.module.Category;
import dev.brot.module.TickModule;

/** Base of every Crystal-category module; all behaviour lives in {@link TickModule}. */
public abstract class CrystalModule extends TickModule {
    protected CrystalModule(String name, String description) { super(name, description, Category.CRYSTAL); }
}
