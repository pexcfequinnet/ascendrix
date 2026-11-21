package org.example.ascendrix.model.mode.shared;

public enum RollPhase {
    NORMAL,
    FADING,
    INVISIBLE;

    public interface GradeScale {
        String label();
        double threshold();
    }
}