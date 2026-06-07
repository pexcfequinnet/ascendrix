package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveHandlingTable {
    private static long frame_to_ns(double frames) {
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }

    public static long getDAS(int level) {
        if (level == 1500) return frame_to_ns(3.5);
        if (level >= 1300) return frame_to_ns(4);
        if (level >= 200) return frame_to_ns(5);
        if (level >= 100) return frame_to_ns(6.5);
        return frame_to_ns(7);
    }
    public static long getARR(int level) {
        if (level >= 1300) return frame_to_ns(0.5);
        if (level >= 200) return frame_to_ns(0.75);
        if (level >= 100) return frame_to_ns(1);
        return frame_to_ns(1.25);
    }
}
