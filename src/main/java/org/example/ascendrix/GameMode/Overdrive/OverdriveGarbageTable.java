package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveGarbageTable {
    public static final long PAUSED = -1L;

    public static long getInterval(int level) {
        if (level == 1500) return PAUSED;
        if (level >= 1400) return seconds(0.5);
        if (level >= 1300) return seconds(0.75);
        if (level >= 1000) return PAUSED;
        if (level >= 900)  return seconds(1.0);
        if (level >= 700)  return seconds(1.5);
        if (level >= 600)  return seconds(2.0);
        if (level >= 500)  return seconds(4.0);
        return PAUSED; // below 500, no garbage
    }

    private static long seconds(double s) {
        return Math.round(s * 1_000_000_000.0);
    }
}
