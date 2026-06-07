package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveRegretTable {
    // Returns the REGRET time limit in nanoseconds for a given section start level
    public static long getRegretLimit(int sectionStart) {
        if (sectionStart >= 900) return seconds(35);
        if (sectionStart >= 700) return seconds(35);
        if (sectionStart >= 600) return seconds(40);
        if (sectionStart >= 500) return seconds(45);
        if (sectionStart >= 400) return seconds(45);
        if (sectionStart >= 300) return seconds(53);
        return seconds(60); // 0-299
    }

    private static long seconds(double s) {
        return Math.round(s * 1_000_000_000.0);
    }
}