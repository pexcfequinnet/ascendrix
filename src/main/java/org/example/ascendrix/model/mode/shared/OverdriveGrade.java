package org.example.ascendrix.model.mode.shared;

public enum OverdriveGrade {
    G1("1"),
    S1("S1"),
    S2("S2"),
    S3("S3"),
    S4("S4"),
    S5("S5"),
    S6("S6"),
    S7("S7"),
    S8("S8"),
    S9("S9"),
    S10("S10"),
    S11("S11"),
    S12("S12"),
    S13("S13"),
    S14("S14"),
    S15("S15"),
    S15_PLUS("S15+");

    public final String label;

    OverdriveGrade(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }

    public boolean isMax() {
        return this == S15_PLUS;
    }

    public OverdriveGrade next() {
        OverdriveGrade[] values = values();
        int next = ordinal() + 1;
        return next < values.length ? values[next] : this;
    }

    public static OverdriveGrade fromLabel(String label) {
        if (label == null || label.isBlank()) return G1;
        for (OverdriveGrade grade : values()) {
            if (grade.label.equalsIgnoreCase(label.trim())) {
                return grade;
            }
        }
        return G1;
    }
}