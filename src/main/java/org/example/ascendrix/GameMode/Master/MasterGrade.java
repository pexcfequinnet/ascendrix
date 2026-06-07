package org.example.ascendrix.GameMode.Master;

public enum MasterGrade implements MasterRollPhase.GradeScale {
    G9("9",  0.40),
    G8("8",  0.45),
    G7("7",  0.50),
    G6("6",  0.55),
    G5("5",  0.60),
    G4("4",  0.65),
    G3("3",  0.70),
    G2("2",  0.75),
    G1("1",  0.80),
    S1("S1", 0.90),
    S2("S2", 0.95),
    S3("S3", 1.00),
    S4("S4", 1.05),
    S5("S5", 1.10),
    S6("S6", 1.15),
    S7("S7", 1.20),
    S8("S8", 1.25),
    S9("S9", 1.30),
    m1("m1", 1.40),
    m2("m2", 1.50),
    m3("m3", 1.60),
    m4("m4", 1.70),
    m5("m5", 1.80),
    m6("m6", 1.90),
    m7("m7", 2.00),
    m8("m8", 2.10),
    m9("m9", 2.20),
    M("Master", 1.50);

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
