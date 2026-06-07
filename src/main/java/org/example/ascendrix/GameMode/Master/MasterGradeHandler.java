package org.example.ascendrix.GameMode.Master;

import org.example.ascendrix.Rotation.SpinType;

public class MasterGradeHandler {
    private MasterRollPhase rollPhase;
    private double grade = 0;
    private double comboBonus = 0.002;
    int combo = 0;
    private MasterGrade currentGrade = MasterGrade.G9;
    private MasterRollGrade currentRollGrade = MasterRollGrade.MK;

    private static final double[][] LINE_CLEAR_VALUES = {
            {0.1, 0.2, 0.4, 0.6}, // Normal
            {0.2, 0.4, 0.6, 0.8}, // Fading roll
            {0.4, 0.6, 0.8, 1.2}  // Invisible roll
    };
    private static final double[] DECAY_LEVEL = {0, (double)1/7, (double)1/3, 0.04};
    private static final double[] SPEED_LEVEL = {1, 1.2, 1.4, 1.6};


    public void decay(int tier, MasterRollPhase phase) {
        double floor = (phase == MasterRollPhase.NORMAL)
                ? getFloor(MasterGrade.values(), currentGrade)
                : getFloor(MasterRollGrade.values(), currentRollGrade);
        grade = Math.max(floor, grade - DECAY_LEVEL[tier - 1]);
    }

    public void calculate(int lines, SpinType spin, int tier, MasterRollPhase phase) {
        if (lines == 0) { combo = 0; return; }

        applyCombo(lines);

        if (lines >= 2) {
            combo++;
            comboBonus = Math.min(comboBonus + 0.0005, 0.004);
            grade += comboBonus;
        }

        double spinBonus = switch(spin) {
            case T_SPIN -> phase == MasterRollPhase.INVISIBLE ? 0.04 : 0.02;
            case NONE     -> 0.0;
            default   -> phase == MasterRollPhase.INVISIBLE ? 0.015 : 0.01;
        };

        double multiplier = (phase == MasterRollPhase.NORMAL) ? SPEED_LEVEL[tier - 1] : 1.0;
        grade += (LINE_CLEAR_VALUES[phase.ordinal()][lines - 1] + spinBonus) * multiplier;

        updateGrade(phase);
    }

    private void updateGrade(MasterRollPhase phase) {
        if (phase == MasterRollPhase.NORMAL) {
            MasterGrade newGrade = fromValue(MasterGrade.values(), grade);
            currentGrade = newGrade;
        }
        else {
            currentRollGrade = fromValue(MasterRollGrade.values(), grade);
            // cap at MM for fading, GM for invisible
            MasterRollGrade cap = phase == MasterRollPhase.FADING
                    ? MasterRollGrade.MM : MasterRollGrade.GM;
            if (currentRollGrade.ordinal() > cap.ordinal())
                currentRollGrade = cap;
        }
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

    public double getProgressToNextGrade() {
        updateGrade(rollPhase); // ensure currentGrade is up to date
        double floor = getCurrentFloor();
        double threshold = (rollPhase == MasterRollPhase.NORMAL)
                ? currentGrade.threshold()
                : currentRollGrade.threshold();
        return Math.clamp((grade - floor) / threshold, 0.0, 1.0);
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
    public double getGradeValue()                { return grade; }
    public void addGradeValue(double amount) {
        this.grade += amount;
        updateGrade(rollPhase);
    }

    public double getGradeOverflow() {
        double floor = getFloor(MasterGrade.values(), MasterGrade.M);
        double ceiling = floor + MasterGrade.M.threshold();
        return Math.max(0, grade - ceiling);
    }

    public void setPhase(MasterRollPhase phase, double overflow) {
        this.rollPhase = phase;
        this.grade = overflow; // start roll grade from overflow
        updateGrade(phase);
    }
    public double getGrade() { return grade; }
    public void applyRegret() {
        MasterGrade previous = currentGrade.ordinal() > 0
                ? MasterGrade.values()[currentGrade.ordinal() - 1]
                : MasterGrade.G9;
        grade = getFloor(MasterGrade.values(), previous);
        currentGrade = previous;
    }
}
