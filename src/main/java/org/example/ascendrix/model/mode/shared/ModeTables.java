package org.example.ascendrix.model.mode.shared;

import org.example.ascendrix.model.mechanics.ARETimer;
import org.example.ascendrix.model.mechanics.GravityModel;
import org.example.ascendrix.model.mechanics.HandlingConfig;

public final class ModeTables {

    private static final double MASTER_SECTION_LEVEL_SPAN = 300.0;
    private static final long INSTANT_20G_FALL_NS         = 1L;

    private ModeTables() {
        // Utility class for mode speed/timing tables
    }

    public static long framesToNs(double frames) {
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }

    public static long secondsToNs(double seconds) {
        return Math.round(seconds * 1_000_000_000.0);
    }

    /* ========================== SHARED RECORD TYPES ========================== */

    public record AREEntry(int speedLevel, long spawnNs, long lineClearNs, long clearAnimNs) {}

    public record HandlingEntry(int speedLevel, long dasNs, long arrNs) {}

    public record FadeEntry(int minLevel, long delayNs, long durationNs) {}

    /* ========================== MASTER 1.0 ARE TABLE ========================== */

    private static final AREEntry[] MASTER_ARE_TABLE = {
            new AREEntry(1, framesToNs(20), framesToNs(18), framesToNs(22)), // Speed Lv 1
            new AREEntry(2, framesToNs(14), framesToNs(12), framesToNs(16)), // Speed Lv 2
            new AREEntry(3, framesToNs(10), framesToNs(8),  framesToNs(12)), // Speed Lv 3
            new AREEntry(4, framesToNs(8),  framesToNs(6),  framesToNs(8))   // Speed Lv 4
    };

    public static AREEntry getMasterARE(int speedLevel) {
        AREEntry result = MASTER_ARE_TABLE[0];
        for (AREEntry entry : MASTER_ARE_TABLE) {
            if (speedLevel >= entry.speedLevel()) {
                result = entry;
            }
        }
        return result;
    }

    public static long getMasterSpawnDelay(int speedLevel) {
        return getMasterARE(speedLevel).spawnNs();
    }

    public static long getMasterLineClearDelay(int speedLevel) {
        return getMasterARE(speedLevel).lineClearNs();
    }

    public static long getMasterClearAnimDelay(int speedLevel) {
        return getMasterARE(speedLevel).clearAnimNs();
    }

    public static void applyMasterARE(ARETimer are, int speedLevel) {
        AREEntry entry = getMasterARE(speedLevel);
        are.setSpawnDelayNs(entry.spawnNs());
        are.setLineClearDelayNs(entry.lineClearNs());
        are.setClearAnimNs(entry.clearAnimNs());
    }

    /* ========================== MASTER 1.0 GRAVITY TABLE ========================== */

    private static final long[][] MASTER_GRAVITY_TABLE = {
            // {start fallNs, end fallNs} per speed level
            {1_000_000_000L, 165_000_000L}, // Spd Lv 1: Marathon Lv 1 -> Lv 10
            {47_000_000L,    11_000_000L},  // Spd Lv 2: Marathon Lv 15 -> Lv 20
            {1_000_000L,     833_000L},     // Spd Lv 3: near 20G
            {1L,             1L}            // Spd Lv 4: 20G
    };

    public static long getMasterGravity(int speedLevel, int levelWithinSpeedLevel) {
        long[] entry = MASTER_GRAVITY_TABLE[Math.clamp(speedLevel - 1, 0, MASTER_GRAVITY_TABLE.length - 1)];
        double t = Math.clamp(levelWithinSpeedLevel / MASTER_SECTION_LEVEL_SPAN, 0.0, 1.0);
        return (long) (entry[0] + t * (entry[1] - entry[0]));
    }

    public static long getMasterGravityWithMultiplier(int speedLevel, int levelWithinSection) {
        long base = getMasterGravity(speedLevel, levelWithinSection);
        if (base <= INSTANT_20G_FALL_NS) {
            return INSTANT_20G_FALL_NS;
        }
        double multiplier = switch (speedLevel) {
            case 2 -> 1.2;
            case 3 -> 1.4;
            case 4 -> 1.6;
            default -> 1.0;
        };
        return Math.max((long) (base / multiplier), INSTANT_20G_FALL_NS);
    }

    public static void applyMasterGravity(GravityModel gravity, int speedLevel, int levelWithinSection) {
        long fallNs = getMasterGravityWithMultiplier(speedLevel, levelWithinSection);
        if (fallNs <= INSTANT_20G_FALL_NS) {
            gravity.setG(20.0);
        } else {
            gravity.setFallNs(fallNs);
        }
    }

    /* ========================== MASTER 1.0 HANDLING TABLE ========================== */

    private static final HandlingEntry[] MASTER_HANDLING_TABLE = {
            new HandlingEntry(1, framesToNs(12),  framesToNs(1)),    // Speed Lv 1
            new HandlingEntry(2, framesToNs(9),   framesToNs(0.75)), // Speed Lv 2
            new HandlingEntry(3, framesToNs(7.5), framesToNs(0.5)),  // Speed Lv 3
            new HandlingEntry(4, framesToNs(5.5), framesToNs(0.25))  // Speed Lv 4
    };

    public static HandlingEntry getMasterHandling(int speedLevel) {
        HandlingEntry result = MASTER_HANDLING_TABLE[0];
        for (HandlingEntry entry : MASTER_HANDLING_TABLE) {
            if (speedLevel >= entry.speedLevel()) {
                result = entry;
            }
        }
        return result;
    }

    public static void applyMasterHandling(HandlingConfig config, int speedLevel) {
        if (config == null) return;
        HandlingEntry entry = getMasterHandling(speedLevel);
        config.setDasNs(entry.dasNs());
        config.setArrNs(entry.arrNs());
    }

    /* ========================== MASTER 1.1 SPEED TIERS ========================== */

    public static int getMaster11SpeedLevel(int level) {
        if (level >= 1500) return 7; // Last Roll
        if (level >= 1200) return 6; // 1200-1499
        if (level >= 1000) return 5; // 1000-1199
        if (level >= 700)  return 4; // 700-999
        if (level >= 500)  return 3; // 500-699
        if (level >= 300)  return 2; // 300-499
        return 1;                    // 0-299
    }

    /* ========================== MASTER 1.1 ARE TABLE ========================== */

    private static final AREEntry[] MASTER_11_ARE_TABLE = {
            new AREEntry(1, framesToNs(16), framesToNs(12),  framesToNs(14)),  // Lv 1 (0-299)
            new AREEntry(2, framesToNs(14), framesToNs(9),   framesToNs(11)),  // Lv 2 (300-499)
            new AREEntry(3, framesToNs(12), framesToNs(6),   framesToNs(8)),   // Lv 3 (500-699)
            new AREEntry(4, framesToNs(10), framesToNs(4.5), framesToNs(6)),   // Lv 4 (700-999)
            new AREEntry(5, framesToNs(8),  framesToNs(3.5), framesToNs(4.5)), // Lv 5 (1000-1199)
            new AREEntry(6, framesToNs(6),  framesToNs(2.5), framesToNs(3)),   // Lv 6 (1200-1499)
            new AREEntry(7, framesToNs(6),  framesToNs(1.5), framesToNs(2))    // Lv 7 (1500 Roll)
    };

    public static AREEntry getMaster11ARE(int speedLevel) {
        AREEntry result = MASTER_11_ARE_TABLE[0];
        for (AREEntry entry : MASTER_11_ARE_TABLE) {
            if (speedLevel >= entry.speedLevel()) {
                result = entry;
            }
        }
        return result;
    }

    public static long getMaster11SpawnDelay(int speedLevel) {
        return getMaster11ARE(speedLevel).spawnNs();
    }

    public static long getMaster11LineClearDelay(int speedLevel) {
        return getMaster11ARE(speedLevel).lineClearNs();
    }

    public static long getMaster11ClearAnimDelay(int speedLevel) {
        return getMaster11ARE(speedLevel).clearAnimNs();
    }

    public static void applyMaster11ARE(ARETimer are, int speedLevel) {
        AREEntry entry = getMaster11ARE(speedLevel);
        are.setSpawnDelayNs(entry.spawnNs());
        are.setLineClearDelayNs(entry.lineClearNs());
        are.setClearAnimNs(entry.clearAnimNs());
    }

    /* ========================== MASTER 1.1 HANDLING TABLE ========================== */

    private static final HandlingEntry[] MASTER_11_HANDLING_TABLE = {
            new HandlingEntry(1, framesToNs(10),  framesToNs(1.0)),  // Lv 1 (0-299)
            new HandlingEntry(2, framesToNs(8.5), framesToNs(0.75)), // Lv 2 (300-499)
            new HandlingEntry(3, framesToNs(7.0), framesToNs(0.5)),  // Lv 3 (500-699)
            new HandlingEntry(4, framesToNs(6.0), framesToNs(0.5)),  // Lv 4 (700-999)
            new HandlingEntry(5, framesToNs(5.5), framesToNs(0.35)), // Lv 5 (1000-1199)
            new HandlingEntry(6, framesToNs(5.0), framesToNs(0.25)), // Lv 6 (1200-1499)
            new HandlingEntry(7, framesToNs(4.5), framesToNs(0.25))  // Lv 7 (1500 Roll)
    };

    public static HandlingEntry getMaster11Handling(int speedLevel) {
        HandlingEntry result = MASTER_11_HANDLING_TABLE[0];
        for (HandlingEntry entry : MASTER_11_HANDLING_TABLE) {
            if (speedLevel >= entry.speedLevel()) {
                result = entry;
            }
        }
        return result;
    }

    public static void applyMaster11Handling(HandlingConfig config, int speedLevel) {
        if (config == null) return;
        HandlingEntry entry = getMaster11Handling(speedLevel);
        config.setDasNs(entry.dasNs());
        config.setArrNs(entry.arrNs());
    }

    /* ========================== MASTER 1.1 GRAVITY, LOCK & GARBAGE ========================== */

    public static void applyMaster11Gravity(GravityModel gravity, int level) {
        if (gravity == null) return;
        gravity.setG(20.0);
    }

    public static long getMaster11LockDelay(int speedLevel) {
        return switch (speedLevel) {
            case 1  -> 650_000_000L; // 0-299
            case 2  -> 550_000_000L; // 300-499
            case 3  -> 450_000_000L; // 500-699
            case 4  -> 375_000_000L; // 700-999
            case 5  -> 300_000_000L; // 1000-1199
            case 6  -> 250_000_000L; // 1200-1499
            default -> 200_000_000L; // 1500+ Roll
        };
    }

    public static long getMaster11GarbageInterval(int level) {
        // Overdrive Lite: light garbage only in 500-999, paused before 500 and during 1000+
        if (level >= 1000) return GarbageSpawner.PAUSED;
        if (level >= 700)  return secondsToNs(4.0);
        if (level >= 500)  return secondsToNs(6.0);
        return GarbageSpawner.PAUSED;
    }

    /* ========================== MASTER 1.1 REGRET TABLE ========================== */

    public static final long MASTER_11_NO_REGRET_LIMIT = -1L;

    public static long getMaster11RegretLimit(int sectionStart) {
        if (sectionStart >= 1000) return MASTER_11_NO_REGRET_LIMIT;
        if (sectionStart >= 700)  return secondsToNs(38);
        if (sectionStart >= 500)  return secondsToNs(45);
        if (sectionStart >= 300)  return secondsToNs(53);
        return secondsToNs(60); // 0-299
    }

    public static double getMaster11RegretLimitSeconds(int sectionStart) {
        long limitNs = getMaster11RegretLimit(sectionStart);
        return (limitNs < 0L) ? -1.0 : limitNs / 1_000_000_000.0;
    }

    /* ========================== OVERDRIVE ARE TABLE ========================== */

    public static long getOverdriveSpawnDelay(int level) {
        if (level >= 300) return framesToNs(6);
        return framesToNs(12);
    }

    public static long getOverdriveLineClearDelay(int level) {
        if (level >= 1500) return framesToNs(1.25);
        if (level >= 500)  return framesToNs(2.5);
        if (level >= 200)  return framesToNs(3.25);
        if (level >= 100)  return framesToNs(4.5);
        return framesToNs(5);
    }

    public static long getOverdriveClearAnimDelay(int level) {
        if (level >= 1500) return framesToNs(1);
        if (level >= 500)  return framesToNs(2);
        if (level >= 200)  return framesToNs(3.25);
        if (level >= 100)  return framesToNs(4.5);
        return framesToNs(5);
    }

    public static void applyOverdriveARE(ARETimer are, int level) {
        are.setSpawnDelayNs(getOverdriveSpawnDelay(level));
        are.setLineClearDelayNs(getOverdriveLineClearDelay(level));
        are.setClearAnimNs(getOverdriveClearAnimDelay(level));
    }

    /* ========================== OVERDRIVE HANDLING TABLE ========================== */

    public static long getOverdriveDAS(int level) {
        if (level >= 1500) return framesToNs(4.5);
        if (level >= 1000) return framesToNs(5.0);
        if (level >= 500)  return framesToNs(5.5);
        if (level >= 400)  return framesToNs(6.0);
        if (level >= 200)  return framesToNs(6.5);
        return framesToNs(7.0);
    }

    public static long getOverdriveARR(int level) {
        if (level >= 1500) return framesToNs(0.25);
        if (level >= 1000) return framesToNs(0.5);
        return framesToNs(0.75);
    }

    public static void applyOverdriveHandling(HandlingConfig config, int level) {
        if (config == null) return;
        config.setDasNs(getOverdriveDAS(level));
        config.setArrNs(getOverdriveARR(level));
    }

    /* ========================== OVERDRIVE GARBAGE TABLE ========================== */

    public static long getOverdriveGarbageInterval(int level) {
        // Last Roll (2000): Invisible + Garbage survival
        if (level >= 2000) return secondsToNs(1.5);
        // Sections 10e-19e (1000-1999): Monochrome & Fading phases have no garbage
        if (level >= 1000) return GarbageSpawner.PAUSED;
        // Sections 5e-9e (500-999): Clean stack + speed test
        if (level >= 900)  return secondsToNs(2.0);
        if (level >= 700)  return secondsToNs(3.0);
        if (level >= 600)  return secondsToNs(4.0);
        if (level >= 500)  return secondsToNs(5.0);
        return GarbageSpawner.PAUSED; // Sections 0e-4e (0-499): Speed test only
    }

    /* ========================== OVERDRIVE LOCK, GRAVITY & FADE ========================== */

    public static long getOverdriveLockDelay(int level) {
        if (level >= 2000) return 150_000_000L;
        if (level >= 1500) return 200_000_000L;
        if (level >= 1000) return 250_000_000L;
        if (level >= 500)  return 400_000_000L;
        return 600_000_000L;
    }

    public static void applyOverdriveGravity(GravityModel gravity, int level) {
        if (gravity == null) return;
        gravity.setG(20.0);
    }

    private static final FadeEntry[] OVERDRIVE_FADE_TABLE = {
            new FadeEntry(1500, secondsToNs(3.0), secondsToNs(0.6)), // 1500-1599 (15e)
            new FadeEntry(1600, secondsToNs(2.2), secondsToNs(0.5)), // 1600-1699 (16e)
            new FadeEntry(1700, secondsToNs(1.5), secondsToNs(0.4)), // 1700-1799 (17e)
            new FadeEntry(1800, secondsToNs(0.9), secondsToNs(0.3)), // 1800-1899 (18e)
            new FadeEntry(1900, secondsToNs(0.4), secondsToNs(0.2))  // 1900-1999 (19e)
    };

    public static FadeEntry getOverdriveFade(int level) {
        FadeEntry result = OVERDRIVE_FADE_TABLE[0];
        for (FadeEntry entry : OVERDRIVE_FADE_TABLE) {
            if (level >= entry.minLevel()) {
                result = entry;
            }
        }
        return result;
    }

    /* ========================== OVERDRIVE REGRET TABLE ========================== */

    public static final long OVERDRIVE_NO_REGRET_LIMIT = -1L;

    public static long getOverdriveRegretLimit(int sectionStart) {
        if (sectionStart >= 1000) return OVERDRIVE_NO_REGRET_LIMIT;
        if (sectionStart >= 900)  return secondsToNs(30);
        if (sectionStart >= 700)  return secondsToNs(35);
        if (sectionStart >= 600)  return secondsToNs(40);
        if (sectionStart >= 400)  return secondsToNs(45);
        if (sectionStart >= 300)  return secondsToNs(53);
        return secondsToNs(60); // 0-299
    }

    public static double getOverdriveRegretLimitSeconds(int sectionStart) {
        long limitNs = getOverdriveRegretLimit(sectionStart);
        return (limitNs < 0L) ? -1.0 : limitNs / 1_000_000_000.0;
    }
}