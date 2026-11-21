package org.example.ascendrix.model.mode.shared;

import java.util.Optional;

public enum RollGrade implements RollPhase.GradeScale {
    MK("MasterK", 1.2),
    MV("MasterV", 1.2),
    MO("MasterO", 1.2),
    MM("MasterM", 2.4),
    GM("Grand Master", 3.6);

    public final String label;
    public final double threshold;

    RollGrade(String label, double threshold) {
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

    public static Optional<RollGrade> fromLabel(String label) {
        if (label == null || label.isBlank()) return Optional.empty();
        for (RollGrade grade : values()) {
            if (grade.label.equalsIgnoreCase(label.trim())) {
                return Optional.of(grade);
            }
        }
        return Optional.empty();
    }
}