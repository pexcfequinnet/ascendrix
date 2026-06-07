package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveLockTable {

    public static long getLockDelay(int level) {
        if (level >= 1500) return 250_000_000L;
        if (level >= 1300) return 166_666_667L;
        if (level >= 1100) return 200_000_000L;
        if (level >= 600)  return 233_333_333L;
        if (level >= 500)  return 250_000_000L;
        if (level >= 300)  return 283_333_333L;
        if (level >= 200)  return 316_666_667L;
        return 350_000_000L;
    }
}
