package dev.brot.hud;

import net.minecraft.client.MinecraftClient;

/** Cheap per-tick measurements for the watermark: speed (BPS) and server TPS. Call {@link #tick} once per client tick. */
public final class HudStats {
    private HudStats() {}
    private static double bps, tps = 20.0, lastX, lastZ;
    private static boolean havePos;
    // TPS estimate on servers: the client advances world time by 1 per tick, the server's time packets overwrite it.
    // A mismatch at a packet means the server ran slower (or faster) than 20 TPS since the previous packet.
    private static long lastTime = Long.MIN_VALUE, syncTime, syncMs;

    public static double bps() { return bps; }
    public static double tps() { return tps; }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) { havePos = false; lastTime = Long.MIN_VALUE; bps = 0; tps = 20.0; return; }
        double x = mc.player.getX(), z = mc.player.getZ();
        double inst = havePos ? Math.hypot(x - lastX, z - lastZ) * 20.0 : 0;
        bps = bps * 0.5 + inst * 0.5;
        lastX = x; lastZ = z; havePos = true;

        var server = mc.getServer();
        if (server != null) {   // singleplayer: the integrated server knows its own tick time
            float avg = server.getAverageTickTime();
            tps = avg <= 50f ? 20.0 : 1000.0 / avg;
            return;
        }
        long now = System.nanoTime() / 1_000_000L, t = mc.world.getTime();
        if (lastTime != Long.MIN_VALUE && t != lastTime + 1 && syncMs != 0) {      // a time packet arrived
            double sec = (now - syncMs) / 1000.0;
            if (sec > 0.5) tps = Math.max(0, Math.min(20.0, (t - syncTime) / sec));
            syncTime = t; syncMs = now;
        } else if (syncMs == 0) { syncTime = t; syncMs = now; }
        else if (now - syncMs > 3000) tps = 20.0;                                   // no mismatch for 3 s: server keeps up
        lastTime = t;
    }
}
