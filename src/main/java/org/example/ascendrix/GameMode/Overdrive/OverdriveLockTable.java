package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveLockTable {

    public static long getLockDelay(int level) {
        if (level >= 1500) return 200_000_000L;
        if (level >= 1100) return 375_000_000L;
        if (level >= 500)  return 350_000_000L;
        return 600_000_000L;
    }
}
