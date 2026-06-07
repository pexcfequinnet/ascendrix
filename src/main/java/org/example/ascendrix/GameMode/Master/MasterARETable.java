package org.example.ascendrix.GameMode.Master;

public class MasterARETable {
    private record AREEntry(int speedLevel, long spawnNs, long lineClearNs, long clearAnimNs) {}

    private static long frame_to_ns(double frames) {
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }

    private static final AREEntry[] TABLE = {
            new AREEntry(1, frame_to_ns(20), frame_to_ns(18), frame_to_ns(22)),  // Spd lv1
            new AREEntry(2, frame_to_ns(14), frame_to_ns(12), frame_to_ns(16)),  // Spd lv2
            new AREEntry(3, frame_to_ns(10), frame_to_ns(8),  frame_to_ns(12)),  // Spd lv3
            new AREEntry(4, frame_to_ns(8),  frame_to_ns(6),  frame_to_ns(8))    // Spd lv4
    };

    public static long getSpawnDelay(int level) {
        return getEntry(level).spawnNs();
    }

    public static long getLineClearDelay(int level) {
        return getEntry(level).lineClearNs();
    }

    private static AREEntry getEntry(int speedLevel) {
        AREEntry result = TABLE[0];
        for (AREEntry entry : TABLE)
            if (speedLevel >= entry.speedLevel()) result = entry;
        return result;
    }
    public static long getClearAnimDelay(int speedLevel) {
        return getEntry(speedLevel).clearAnimNs();
    }
}
