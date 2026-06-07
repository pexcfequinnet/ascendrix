package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveHandlingTable {
    private static long frame_to_ns(double frames) {
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }

    public static long getDAS(int level) {
        if (level == 1500) return frame_to_ns(4.5);
        if (level >= 1200) return frame_to_ns(5);
        if (level >= 500) return frame_to_ns(5.5);
        if (level >= 400) return frame_to_ns(6.25);
        if (level >= 200) return frame_to_ns(6.75);
        return frame_to_ns(7.5);
    }
    public static long getARR(int level) {
        if (level >= 1500) return frame_to_ns(0.25);
        if (level >= 1200) return frame_to_ns(0.5);
        if (level >= 500) return frame_to_ns(0.75);
        return frame_to_ns(1);
    }
}
