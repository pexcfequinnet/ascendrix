package org.example.ascendrix.model.mode.shared;

import java.util.Arrays;

public class OverdriveSectionTracker {

    public enum SectionResult {
        PASS,
        REGRET
    }

    public record SectionData(int start, int end, double regretLimitSeconds) {}

    private static final SectionData[] SECTIONS = {
            new SectionData(0,    99,   60.0),
            new SectionData(100,  199,  60.0),
            new SectionData(200,  299,  60.0),
            new SectionData(300,  399,  53.0),
            new SectionData(400,  499,  45.0),
            new SectionData(500,  599,  45.0),
            new SectionData(600,  699,  40.0),
            new SectionData(700,  799,  35.0),
            new SectionData(800,  899,  35.0),
            new SectionData(900,  999,  30.0),
            new SectionData(1000, 1099, -1.0),
            new SectionData(1100, 1199, -1.0),
            new SectionData(1200, 1299, -1.0),
            new SectionData(1300, 1399, -1.0),
            new SectionData(1400, 1499, -1.0),
            new SectionData(1500, 1599, -1.0),
            new SectionData(1600, 1699, -1.0),
            new SectionData(1700, 1799, -1.0),
            new SectionData(1800, 1899, -1.0),
            new SectionData(1900, 1999, -1.0),
    };

    private final boolean[] regrets = new boolean[SECTIONS.length];

    public SectionResult evaluate(int sectionIndex, double sectionTime, OverdriveGradeTracker gradeTracker) {
        if (sectionIndex < 0 || sectionIndex >= SECTIONS.length) {
            return SectionResult.PASS;
        }

        SectionData section = SECTIONS[sectionIndex];
        if (section.regretLimitSeconds >= 0.0 && sectionTime > section.regretLimitSeconds) {
            regrets[sectionIndex] = true;
            gradeTracker.onRegret();
            return SectionResult.REGRET;
        }

        gradeTracker.onSectionPass();
        return SectionResult.PASS;
    }

    public int getSectionIndex(int level) {
        for (int i = 0; i < SECTIONS.length; i++) {
            if (level >= SECTIONS[i].start && level <= SECTIONS[i].end) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasRegret(int idx) {
        return idx >= 0 && idx < regrets.length && regrets[idx];
    }

    public int getRegretCount() {
        int count = 0;
        for (boolean r : regrets) {
            if (r) count++;
        }
        return count;
    }

    public void reset() {
        Arrays.fill(regrets, false);
    }
}