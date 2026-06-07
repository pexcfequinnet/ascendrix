package org.example.ascendrix.GameMode.Master;

public enum MasterRollGrade implements MasterRollPhase.GradeScale {

    MK("MasterK", 1.2),
    MV("MasterV", 1.2),
    MO("MasterO", 1.2),
    MS("MasterS", 1.2),
    MM("MasterM", 2.4),
    GM("Grand Master", 3.6);


    public final String label;
    public final double threshold;

    MasterRollGrade(String label, double threshold) {
        this.label = label;
        this.threshold = threshold;
    }
    @Override public String label()     { return label; }
    @Override public double threshold() { return threshold; }
}


