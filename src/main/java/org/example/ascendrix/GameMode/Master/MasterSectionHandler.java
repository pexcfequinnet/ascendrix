package org.example.ascendrix.GameMode.Master;

public class MasterSectionHandler {

    public int missedCools() {
        return 1;
    }

    public record SectionData(int start, int end, double timeLimit, int minQuads) {}

    private static final SectionData[] SECTIONS = {
            new SectionData(0,   70,  52.00, 1),
            new SectionData(100, 170, 52.00, 1),
            new SectionData(200, 270, 50.00, 1),
            new SectionData(300, 370, 47.00, 1),
            new SectionData(400, 470, 45.00, 1),
            new SectionData(500, 570, 42.00, 1),
            new SectionData(600, 670, 42.00, 1),
            new SectionData(700, 770, 38.00, 1),
            new SectionData(800, 870, 35.00, 1),
            new SectionData(900, 999, -1,    6)  // -1 = no time limit
    };

    private static final double[] REGRET_LIMITS = {
            90, 75, 75, 68, 60, 60, 50, 50, 50, 50  // in seconds
    };

    private final boolean[] cools = new boolean[SECTIONS.length];
    private final boolean[] regrets = new boolean[SECTIONS.length];
    private double lockedCoolTime = -1;

    public void evaluate(int level, double sectionTime, int quadsInSection) {
        int idx = getSectionIndex(level);
        if (idx == -1) return;

        SectionData section = SECTIONS[idx];

        // REGRET check
        if (sectionTime > REGRET_LIMITS[idx])
            regrets[idx] = true;

        // COOL check
        boolean timeOk = section.timeLimit < 0 || sectionTime <= getCoolThreshold(idx);
        boolean quadsOk = quadsInSection >= section.minQuads;
        if (timeOk && quadsOk)
            cools[idx] = true;

        // Lock cool time for next section
        if (cools[idx])
            lockedCoolTime = sectionTime + 2.0;
    }

    private double getCoolThreshold(int idx) {
        return (lockedCoolTime > 0) ? lockedCoolTime : SECTIONS[idx].timeLimit;
    }

    // Determine if player satisfies all COOLs requirement for invisible roll
    public boolean allCools() {
        for (boolean c : cools) if (!c) return false;
        return true;
    }

    public boolean hasRegret(int idx) { return regrets[idx]; }
    public boolean hasCool(int idx)   { return cools[idx]; }

    private int getSectionIndex(int level) {
        for (int i = 0; i < SECTIONS.length; i++)
            if (level >= SECTIONS[i].start && level <= SECTIONS[i].end) return i;
        return -1;
    }

    public boolean meetsInvisibleRequirements(double totalTime) {
        return allCools() && hasCool(9) && totalTime < 510; // 8:30 in seconds
    }
}
