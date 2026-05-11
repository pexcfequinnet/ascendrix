package org.example.ascendrix;

public class MasterARETable {
    private record AREEntry(int speedLevel, long spawnNs, long lineClearNs, long clearAnimNs) {}

    private static final long F = 1_000_000_000L / 60;

    private static final AREEntry[] TABLE   = {
            new AREEntry(1, 27*F, 27*F, 40*F),  // Spd lv1
            new AREEntry(2, 14*F, 8*F,  20*F),  // Spd lv2
            new AREEntry(3, 10*F,  8*F,  10*F),   // Spd lv3
            new AREEntry(4, 6*F,  6*F,  5*F)    // Spd lv4
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
}
