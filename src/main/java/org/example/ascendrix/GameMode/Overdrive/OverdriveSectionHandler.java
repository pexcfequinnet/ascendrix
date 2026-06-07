package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveSectionHandler {
    public enum SectionResult { PASS, REGRET }
    public record SectionData(int start, int end, double regretLimit) {}
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
            new SectionData(900,  999,  35.0),
            new SectionData(1000, 1099, -1),
            new SectionData(1100, 1199, -1),
            new SectionData(1200, 1299, -1),
            new SectionData(1300, 1399, -1),
            new SectionData(1400, 1499, -1)
    };
    private final boolean[] regrets = new boolean[SECTIONS.length];

    public SectionResult evaluate(int sectionIndex, double sectionTime, OverdriveGradeHandler gradeHandler) {
        if (sectionIndex < 0 || sectionIndex >= SECTIONS.length) return SectionResult.PASS;

        SectionData section = SECTIONS[sectionIndex];

        if (section.regretLimit >= 0 && sectionTime > section.regretLimit) {
            regrets[sectionIndex] = true;
            gradeHandler.onRegret();
            return SectionResult.REGRET;
        }

        gradeHandler.onSectionPass();
        return SectionResult.PASS;
    }

    public int getSectionIndex(int level) {
        for (int i = 0; i < SECTIONS.length; i++)
            if (level >= SECTIONS[i].start && level <= SECTIONS[i].end) return i;
        return -1;
    }

    public boolean hasRegret(int idx)  { return regrets[idx]; }

    public int getRegretCount() {
        int count = 0;
        for (boolean r : regrets) if (r) count++;
        return count;
    }
}
