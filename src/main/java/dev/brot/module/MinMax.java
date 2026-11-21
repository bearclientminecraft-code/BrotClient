package dev.brot.module;

import dev.brot.setting.NumberSetting;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Shared "Min / Max" pair of number settings. Editing either side keeps min <= max (the other side is pushed along),
 * so the invariant also holds after a config load and no module needs its own validation.
 */
public final class MinMax {
    public final NumberSetting min, max;

    public MinMax(Module owner, String minName, String maxName, double defMin, double defMax,
                  double lo, double hi, double step, String unit, String minDesc, String maxDesc) {
        min = owner.add(new NumberSetting(minName, defMin, lo, hi, step).unit(unit), minDesc);   // min must be added first: config load order
        max = owner.add(new NumberSetting(maxName, defMax, lo, hi, step).unit(unit), maxDesc);
        min.onChange = v -> { if (v > max.get()) max.set(v); };
        max.onChange = v -> { if (v < min.get()) min.set(v); };
    }

    public MinMax visibleWhen(Supplier<Boolean> s) { min.visibleWhen(s); max.visibleWhen(s); return this; }

    /** Uniform random value in [min, max]. */
    public double sample() {
        double a = Math.max(0, min.get()), b = Math.max(a, max.get());
        return a == b ? a : ThreadLocalRandom.current().nextDouble(a, b);
    }
    public long sampleMs() { return Math.round(sample()); }
}
