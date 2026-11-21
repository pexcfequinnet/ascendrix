package org.example.ascendrix.model.mode.shared;

public class SectionTracker {

    public enum SectionResult {
        COOL,
        NEUTRAL,
        REGRET
    }

    public record SectionData(int start, int end, double timeLimit, int minQuads) {}

    private static final SectionData[] SECTIONS = {
            new SectionData(0,   70,  52.00, 3),
            new SectionData(100, 170, 52.00, 3),
            new SectionData(200, 270, 50.00, 3),
            new SectionData(300, 370, 47.00, 3),
            new SectionData(400, 470, 45.00, 3),
            new SectionData(500, 570, 42.00, 3),
            new SectionData(600, 670, 42.00, 3),
            new SectionData(700, 770, 38.00, 3),
            new SectionData(800, 870, 35.00, 3),
            new SectionData(900, 999, -1.0,  6) // -1 = relies on lockedCoolTime
    };

    private static final double[] REGRET_LIMITS = {
            90.0, 75.0, 75.0, 68.0, 60.0, 60.0, 50.0, 50.0, 50.0, 50.0 // in seconds
    };

    private static final double LOCKED_COOL_OFFSET_SEC    = 2.0;
    private static final double INVISIBLE_MAX_TOTAL_SEC   = 510.0;

    private final boolean[] evaluated = new boolean[SECTIONS.length];
    private final boolean[] cools     = new boolean[SECTIONS.length];
    private final boolean[] regrets   = new boolean[SECTIONS.length];
    private double lockedCoolTime = -1.0;

    public void evaluate(int level, double sectionTime, int quadsInSection) {
        int idx = getSectionIndex(level);
        if (idx == -1) return;

        evaluated[idx] = true;
        SectionData section = SECTIONS[idx];

        if (sectionTime > REGRET_LIMITS[idx]) {
            regrets[idx] = true;
        }

        boolean timeOk = section.timeLimit < 0 || sectionTime <= getCoolThreshold(idx);
        boolean quadsOk = quadsInSection >= section.minQuads;
        if (timeOk && quadsOk) {
            cools[idx] = true;
            lockedCoolTime = sectionTime + LOCKED_COOL_OFFSET_SEC;
        }
    }

    public SectionResult evaluateSection(int sectionIndex, double sectionTime) {
        if (sectionIndex < 0 || sectionIndex >= SECTIONS.length) {
            return SectionResult.NEUTRAL;
        }

        evaluated[sectionIndex] = true;
        double coolThreshold = getCoolThreshold(sectionIndex);
        double regretThreshold = REGRET_LIMITS[sectionIndex];

        if (sectionTime <= coolThreshold) {
            cools[sectionIndex] = true;
            lockedCoolTime = sectionTime + LOCKED_COOL_OFFSET_SEC;
            return SectionResult.COOL;
        } else if (sectionTime > regretThreshold) {
            regrets[sectionIndex] = true;
            return SectionResult.REGRET;
        }
        return SectionResult.NEUTRAL;
    }

    private double getCoolThreshold(int idx) {
        return (lockedCoolTime > 0.0) ? lockedCoolTime : SECTIONS[idx].timeLimit;
    }

    public boolean allCools() {
        for (boolean c : cools) {
            if (!c) return false;
        }
        return true;
    }

    public boolean hasCool(int idx) {
        return idx >= 0 && idx < cools.length && cools[idx];
    }

    public boolean hasRegret(int idx) {
        return idx >= 0 && idx < regrets.length && regrets[idx];
    }

    public int getCoolCount() {
        int count = 0;
        for (boolean c : cools) {
            if (c) count++;
        }
        return count;
    }

    public int missedCools() {
        int missed = 0;
        for (int i = 0; i < SECTIONS.length; i++) {
            if (evaluated[i] && !cools[i]) {
                missed++;
            }
        }
        return missed;
    }

    public boolean meetsInvisibleRequirements(double totalTime, MasterGrade currentGrade) {
        return allCools()
                && hasCool(9)
                && totalTime < INVISIBLE_MAX_TOTAL_SEC
                && currentGrade == MasterGrade.M;
    }

    private int getSectionIndex(int level) {
        for (int i = 0; i < SECTIONS.length; i++) {
            if (level >= SECTIONS[i].start && level <= SECTIONS[i].end) {
                return i;
            }
        }
        return -1;
    }
}