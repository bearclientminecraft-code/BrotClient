package dev.brot.gui;

import dev.brot.module.*;
import dev.brot.module.Module;
import java.util.*;

/** Selected category, search query and the module list that goes with them. */
public final class CategoryManager {
    public static final List<Category> MAIN = List.of(Category.CRYSTAL, Category.SWORD, Category.MACE, Category.UHC, Category.MOVEMENT);
    public static final List<Category> MISC = List.of(Category.THEMES, Category.HUD);

    public Category selected = Category.CRYSTAL;
    public String search = "";

    public List<Module> visibleModules() {
        if (search.isBlank()) return ModuleManager.in(selected);
        String q = search.toLowerCase(Locale.ROOT);
        return ModuleManager.all().stream()
            .filter(m -> !m.category.misc && m.name.toLowerCase(Locale.ROOT).contains(q)).toList();
    }
}
