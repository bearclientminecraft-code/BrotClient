package dev.brot.anim;

import dev.brot.theme.ThemeManager;

public final class AnimationManager {
    private AnimationManager() {}
    private static long last = System.nanoTime();
    private static float dt = 0.016f;

    /** Call once per frame before updating animations. */
    public static void frame() {
        long now = System.nanoTime();
        dt = Math.min(0.1f, (now - last) / 1e9f);
        last = now;
    }
    public static float dt() { return dt * ThemeManager.animSpeed(); }

    /** Exponentially smoothed value. */
    public static final class Anim {
        public float value, target;
        public Anim(float v) { value = target = v; }
        public void to(float t) { target = t; }
        public float update() {
            value += (target - value) * Math.min(1f, dt() * 14f);
            if (Math.abs(target - value) < 0.002f) value = target;
            return value;
        }
        public boolean done() { return value == target; }
    }
}
