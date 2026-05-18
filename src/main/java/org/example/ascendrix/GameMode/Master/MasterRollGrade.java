package org.example.ascendrix.GameMode.Master;

public enum MasterRollGrade implements MasterRollPhase.GradeScale {

    MK("MasterK", 1),
    MV("MasterV", 2),
    MO("MasterO", 4),
    MS("MasterS", 6),
    MM("MasterM", 9),
    GM("Grand Master", 12);


    public final String label;
    public final double threshold;

    MasterRollGrade(String label, double threshold) {
        this.label = label;
        this.threshold = threshold;
    }
    @Override public String label()     { return label; }
    @Override public double threshold() { return threshold; }
}


