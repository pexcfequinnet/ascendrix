package org.example.ascendrix.GameMode.Overdrive;

public enum OverdriveGradeTable {
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

    OverdriveGradeTable(String label) {
        this.label = label;
    }

    public boolean isMax() {
        return this == S15_PLUS;
    }

    public OverdriveGradeTable next() {
        OverdriveGradeTable[] values = values();
        int next = ordinal() + 1;
        return next < values.length ? values[next] : this;
    }
}
