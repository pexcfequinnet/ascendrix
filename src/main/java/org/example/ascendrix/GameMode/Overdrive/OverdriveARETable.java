package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveARETable {
    private static long frame_to_ns(double frames) {
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }

    public static long getSpawnDelay(int level) {
        if (level >= 300) return frame_to_ns(6);
        return frame_to_ns(12);
    }

    public static long getLineClearDelay(int level) {
        if (level == 1500) return frame_to_ns(1.25);
        if (level >= 500)  return frame_to_ns(2.5);
        if (level >= 200)  return frame_to_ns(3.25);
        if (level >= 100)  return frame_to_ns(4.5);
        return frame_to_ns(5);
    }

    public static long getClearAnimDelay(int level) {
        if (level >= 1500) return frame_to_ns(1);
        if (level >= 500)  return frame_to_ns(2);
        if (level >= 200)  return frame_to_ns(3.25);
        if (level >= 100)  return frame_to_ns(4.5);
        return frame_to_ns(5);
    }

}
