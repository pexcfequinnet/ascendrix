package org.example.ascendrix.GameMode.Master;

public enum MasterRollPhase {
    NORMAL, FADING, INVISIBLE;

    public static interface GradeScale {
        String label();
        double threshold();
    }
}
