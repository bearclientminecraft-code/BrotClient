package dev.brot.module;

import java.util.ArrayDeque;
import java.util.function.BooleanSupplier;

/**
 * Non-blocking step sequence. Each step waits its own delay (ms) AFTER the previous step, runs at most once per tick
 * (so effective resolution is one client tick = 50 ms), and returns false to abort the rest of the sequence.
 */
public final class ActionQueue {
    private record Step(long delayMs, BooleanSupplier action) {}
    private final ArrayDeque<Step> steps = new ArrayDeque<>();
    private long dueAt;

    public boolean running() { return !steps.isEmpty(); }
    public void add(long delayMs, BooleanSupplier action) {
        if (steps.isEmpty()) dueAt = now() + delayMs;
        steps.add(new Step(Math.max(0, delayMs), action));
    }
    public void clear() { steps.clear(); }

    public void tick() {
        if (steps.isEmpty() || now() < dueAt) return;
        Step s = steps.poll();
        boolean ok;
        try { ok = s.action().getAsBoolean(); }
        catch (RuntimeException ex) { System.err.println("[BROT] action failed: " + ex); ok = false; }
        if (!ok) { steps.clear(); return; }
        Step next = steps.peek();
        if (next != null) dueAt = now() + next.delayMs();
    }
    private static long now() { return System.nanoTime() / 1_000_000L; }
}
