package dev.brot.core;

import dev.brot.config.ConfigManager;
import dev.brot.gui.ClickGUI;
import dev.brot.hud.*;
import dev.brot.module.*;
import dev.brot.module.Module;
import dev.brot.module.crystal.CrystalCategory;
import dev.brot.module.impl.PlaceholderModule;
import dev.brot.theme.ThemeManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.lwjgl.glfw.GLFW;

public class BROTClient implements ClientModInitializer {
    private boolean guiKeyDown;

    @Override public void onInitializeClient() {
        dev.brot.render.ShapePipelines.init();   // must run before resources reload
        ThemeManager.init();
        ModuleManager.register(ThemeManager.THEME, HUDManager.MASTER);
        HUDManager.register(new WatermarkElement(), new InfoElement());

        // === Register new modules here. The GUI picks them up automatically. ===
        CrystalCategory.register();
        for (Category c : CategoryManager_MAIN())
            if (c != Category.CRYSTAL) for (String n : new String[]{"Alpha", "Beta", "Gamma"})
                ModuleManager.register(new PlaceholderModule(c.label + " " + n, c));

        ConfigManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ModuleManager.tick(mc);
            if (mc.getWindow() == null) return;
            boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !guiKeyDown && mc.currentScreen == null && mc.player != null) mc.setScreen(new ClickGUI());
            guiKeyDown = down;
        });
        // HudRenderCallback may be deprecated in favour of HudElementRegistry in newer Fabric API: verify.
        HudRenderCallback.EVENT.register((ctx, tick) -> HUDManager.render(ctx));
    }

    private static java.util.List<Category> CategoryManager_MAIN() { return dev.brot.gui.CategoryManager.MAIN; }
}
