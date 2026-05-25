package org.example.ascendrix.GameMode.Master;

import org.example.ascendrix.Rotation.SpinType;

public class MasterGradeHandler {
    private MasterRollPhase rollPhase;
    private double grade = 0;
    private double comboBonus = 0.002;
    private int combo = 0;
    private MasterGrade currentGrade = MasterGrade.G9;
    private MasterRollGrade currentRollGrade = null;

    private static final double[][] LINE_CLEAR_VALUES = {
            {0.1, 0.2, 0.4, 0.6}, // Normal
            {0.2, 0.4, 0.6, 0.8}, // Fading roll
            {0.4, 0.6, 1.8, 1.0}  // Invisible roll
    };
    private static final double[] DECAY_LEVEL = {0, (double)1/7, (double)1/3, 0.04};
    private static final double[] SPEED_LEVEL = {1, 1.2, 1.4, 1.6};


    public void decay(int tier, MasterRollPhase phase) {
        double floor = (phase == MasterRollPhase.NORMAL)
                ? getFloor(MasterGrade.values(), currentGrade)
                : getFloor(MasterRollGrade.values(), currentRollGrade);
        grade = Math.max(floor, grade - DECAY_LEVEL[tier]);
    }

    public void calculate(int lines, SpinType spin, int tier, MasterRollPhase phase) {
        if (lines == 0) { combo = 0; return; }

        if (lines >= 2 && combo > 1)
            grade += comboBonus;
        else {
            combo++;
            comboBonus = Math.min(comboBonus + 0.0005, 0.004);
            grade += comboBonus;
        }

        double spinBonus = switch(spin) {
            case T_SPIN -> 0.02;
            case NONE   -> 0;
            default     -> 0.01;
        };

        double multiplier = (phase == MasterRollPhase.NORMAL) ? SPEED_LEVEL[tier] : 1.0;
        grade += (LINE_CLEAR_VALUES[phase.ordinal()][lines - 1] + spinBonus) * multiplier;

        updateGrade(phase);
    }

    private void updateGrade(MasterRollPhase phase) {
        if (phase == MasterRollPhase.NORMAL)
            currentGrade = fromValue(MasterGrade.values(), grade);
        else
            currentRollGrade = fromValue(MasterRollGrade.values(), grade);
    }

    private <T extends Enum<T> & MasterRollPhase.GradeScale> T fromValue(T[] values, double value) {
        double cumulative = 0;
        for (T g : values) {
            cumulative += g.threshold();
            if (value < cumulative) return g;
        }
        return values[values.length - 1];
    }

    public <T extends Enum<T> & MasterRollPhase.GradeScale> double getFloor(T[] values, T target) {
        double cumulative = 0;
        for (T g : values) {
            if (g == target) return cumulative;
            cumulative += g.threshold();
        }
        return 0;
    }
    public double getCurrentFloor() {
        if (rollPhase == MasterRollPhase.NORMAL)
            return getFloor(MasterGrade.values(), currentGrade);
        else
            return getFloor(MasterRollGrade.values(), currentRollGrade);
    }

    public double getNextFloor() {
        if (rollPhase == MasterRollPhase.NORMAL)
            return getCurrentFloor() + currentGrade.threshold();
        else
            return getCurrentFloor() + currentRollGrade.threshold();
    }
    private void applyCombo(int lines) {
        if (lines >= 2 && combo > 1) {
            grade += comboBonus;
        } else {
            combo++;
            comboBonus = Math.min(comboBonus + 0.0005, 0.004);
            grade += comboBonus;
        }
    }
    public void resetCombo() {
        combo = 0;
        comboBonus = 0.002;
    }

    public MasterGrade getCurrentGrade()         { return currentGrade; }
    public MasterRollGrade getCurrentRollGrade() { return currentRollGrade; }
    public double getGradeValue()                { return grade; }

    public void setPhase(MasterRollPhase phase) { this.rollPhase = phase; }
    public double getGrade() { return grade; }
    public void applyRegret() {
        MasterGrade previous = currentGrade.ordinal() > 0
                ? MasterGrade.values()[currentGrade.ordinal() - 1]
                : MasterGrade.G9;
        grade = getFloor(MasterGrade.values(), previous);
        currentGrade = previous;
    }
    private double getCurrentThreshold() { /* return current rank's floor */ return 0; }
}
