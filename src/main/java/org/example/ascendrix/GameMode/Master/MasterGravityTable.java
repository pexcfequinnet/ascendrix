package org.example.ascendrix.GameMode.Master;

import org.example.ascendrix.Movement.*;

public class MasterGravityTable {

    private static final long[][] TABLE = {
            // {start gravity, end gravity} per speed level
            {1_000_000_000L, 165_000_000L},  // Spd Lv1: Marathon Lv1 → Lv10
            {47_000_000L,    11_000_000L},   // Spd Lv2: Marathon Lv15 → Lv20
            {1_000_000L,     833_000L},    // Spd Lv3: near 20G
            {1L,       1L},      // Spd Lv4: 20G
    };

    public static long getGravityWithMultiplier(int speedLevel, int levelWithinSection) {
        long base = getGravity(speedLevel, levelWithinSection);
        // 20G hard cap
        if (base <= 1L) return 1L;
        double multiplier = switch (speedLevel) {
            case 2 -> 1.2;
            case 3 -> 1.4;
            case 4 -> 1.6;
            default -> 1.0;
        };
        return Math.max((long)(base / multiplier), 0L);
    }

    public static long getGravity(int speedLevel, int levelWithinSpeedLevel) {
        long[] entry = TABLE[Math.clamp(speedLevel - 1, 0, TABLE.length - 1)];
        double t = Math.clamp(levelWithinSpeedLevel / 300.0, 0.0, 1.0);
        return (long)(entry[0] + t * (entry[1] - entry[0]));
    }


}