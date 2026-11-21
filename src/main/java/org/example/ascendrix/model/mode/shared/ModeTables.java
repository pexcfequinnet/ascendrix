package org.example.ascendrix.model.mode.shared;

import org.example.ascendrix.model.mechanics.ARETimer;
import org.example.ascendrix.model.mechanics.GravityModel;
import org.example.ascendrix.model.mechanics.HandlingConfig;

public final class ModeTables {

    private static final double SECTION_LEVEL_SPAN = 300.0;
    private static final long INSTANT_20G_FALL_NS  = 1L;

    private ModeTables() {
        // Utility class for mode speed/timing tables
    }

    public static long framesToNs(double frames) {
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }

    public static long secondsToNs(double seconds) {
        return Math.round(seconds * 1_000_000_000.0);
    }

    /* ========================== MASTER 1.0 ARE TABLE ========================== */

    public record AREEntry(int speedLevel, long spawnNs, long lineClearNs, long clearAnimNs) {}

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
        double t = Math.clamp(levelWithinSpeedLevel / SECTION_LEVEL_SPAN, 0.0, 1.0);
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

    public record HandlingEntry(int speedLevel, long dasNs, long arrNs) {}

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
        if (level >= 1200) return framesToNs(5);
        if (level >= 500)  return framesToNs(5.5);
        if (level >= 400)  return framesToNs(6.0);
        if (level >= 200)  return framesToNs(6.5);
        return framesToNs(7);
    }

    public static long getOverdriveARR(int level) {
        if (level >= 1500) return framesToNs(0.25);
        if (level >= 1200) return framesToNs(0.5);
        return framesToNs(0.75);
    }

    public static void applyOverdriveHandling(HandlingConfig config, int level) {
        if (config == null) return;
        config.setDasNs(getOverdriveDAS(level));
        config.setArrNs(getOverdriveARR(level));
    }

    /* ========================== OVERDRIVE GARBAGE TABLE ========================== */

    public static final long OVERDRIVE_GARBAGE_PAUSED = -1L;

    public static long getOverdriveGarbageInterval(int level) {
        if (level >= 1500) return OVERDRIVE_GARBAGE_PAUSED;
        if (level >= 1400) return secondsToNs(0.5);
        if (level >= 1300) return secondsToNs(0.75);
        if (level >= 1000) return OVERDRIVE_GARBAGE_PAUSED;
        if (level >= 900)  return secondsToNs(1.0);
        if (level >= 700)  return secondsToNs(1.5);
        if (level >= 600)  return secondsToNs(2.0);
        if (level >= 500)  return secondsToNs(4.0);
        return OVERDRIVE_GARBAGE_PAUSED; // Below 500, no garbage
    }
    /* ========================== OVERDRIVE LOCK & GRAVITY TABLES ========================== */

    public static long getOverdriveLockDelay(int level) {
        if (level >= 1500) return 150_000_000L;
        if (level >= 1100) return 200_000_000L;
        if (level >= 500)  return 400_000_000L;
        return 600_000_000L;
    }

    public static void applyOverdriveGravity(GravityModel gravity, int level) {
        if (gravity == null) return;
        // Overdrive defaults to 20G; replace with OverdriveGravityTable curve if pre-20G steps exist
        gravity.setG(20.0);
    }
    /* ========================== OVERDRIVE REGRET TABLE ========================== */

    public static final long OVERDRIVE_NO_REGRET_LIMIT = -1L;

    public static long getOverdriveRegretLimit(int sectionStart) {
        if (sectionStart >= 1000) return OVERDRIVE_NO_REGRET_LIMIT;
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

