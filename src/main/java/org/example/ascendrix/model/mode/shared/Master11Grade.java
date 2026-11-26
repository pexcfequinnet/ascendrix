package org.example.ascendrix.model.mode.shared;

public enum Master11Grade implements RollPhase.GradeScale {
    S1("S1",   1.0),
    S2("S2",   1.0),
    S3("S3",   1.0),
    S4("S4",   1.0),
    S5("S5",   1.0),
    S6("S6",   1.0),
    S7("S7",   1.0),
    S8("S8",   1.0),
    S9("S9",   1.0),
    S10("S10", 1.0),
    S11("S11", 1.0),
    S12("S12", 1.0),
    S13("S13", 1.0),
    S14("S14", 1.0),
    S15("S15", 1.0),
    GM("Grand Master", 0.0);

    public final String label;
    public final double threshold;

    Master11Grade(String label, double threshold) {
        this.label = label;
        this.threshold = threshold;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public double threshold() {
        return this.threshold;
    }

    public boolean isGrandMaster() {
        return this == GM;
    }

    public Master11Grade nextSectionGrade() {
        int nextIdx = ordinal() + 1;
        // Cap section promotions at S15; GM is reserved for clearing the Credit Roll
        if (nextIdx >= GM.ordinal()) {
            return S15;
        }
        return values()[nextIdx];
    }

    public Master11Grade previousGrade() {
        int prevIdx = Math.max(0, ordinal() - 1);
        return values()[prevIdx];
    }

    public static Master11Grade fromLabel(String label) {
        if (label == null || label.isBlank()) return S1;
        String trimmed = label.trim();
        if (trimmed.equalsIgnoreCase("GM")) return GM;
        for (Master11Grade grade : values()) {
            if (grade.label.equalsIgnoreCase(trimmed)) {
                return grade;
            }
        }
        return S1;
    }
}