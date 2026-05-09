package org.example.ascendrix;

public enum MasterGrade implements GradeScale{
    G9("9",  1.5),
    G8("8",  1.5),
    G7("7",  1.5),
    G6("6",  1.5),
    G5("5",  1.5),
    G4("4",  2.0),
    G3("3",  2.0),
    G2("2",  2.0),
    G1("1",  2.0),
    S1("S1", 2.0),
    S2("S2", 2.0),
    S3("S3", 2.5),
    S4("S4", 2.5),
    S5("S5", 2.5),
    S6("S6", 3.0),
    S7("S7", 3.0),
    S8("S8", 3.0),
    S9("S9", 3.0),
    m1("m1", 3.0),
    m2("m2", 3.0),
    m3("m3", 3.5),
    m4("m4", 3.5),
    m5("m5", 3.5),
    m6("m6", 4.0),
    m7("m7", 4.0),
    m8("m8", 4.0),
    m9("m9", 4.0),
    M("Master", 4.0);

    public final String label;
    public final double threshold;

    MasterGrade(String label, double threshold) {
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
}
