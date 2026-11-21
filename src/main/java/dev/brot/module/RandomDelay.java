package dev.brot.module;

import dev.brot.setting.BooleanSetting;

/**
 * The one Random Delay implementation. A module that waits somewhere creates one of these in its constructor and wraps
 * the wait in {@link #apply(long)}: when enabled, a random value from [Minimum Delay, Maximum Delay] is ADDED to the
 * module's own base delay; when disabled the base delay is returned unchanged.
 */
public final class RandomDelay {
    public final BooleanSetting enabled;
    public final MinMax range;

    public RandomDelay(Module owner) {
        enabled = owner.add(new BooleanSetting("Enable Random Delay", false),
            "Adds a random extra wait (between Minimum and Maximum Delay) on top of this module's own delays");
        range = new MinMax(owner, "Minimum Delay", "Maximum Delay", 0, 50, 0, 1000, 5, "ms",
            "Smallest extra random wait", "Largest extra random wait").visibleWhen(enabled::get);
    }

    public long apply(long baseMs) {
        return Math.max(0, baseMs) + (enabled.get() ? range.sampleMs() : 0);
    }
}
