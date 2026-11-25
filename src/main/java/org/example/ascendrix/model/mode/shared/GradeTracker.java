package org.example.ascendrix.model.mode.shared;

import org.example.ascendrix.model.mechanics.rotation.SpinType;

public class GradeTracker {

    private static final double[][] LINE_CLEAR_VALUES = {
            {0.1, 0.2, 0.4, 0.6}, // NORMAL
            {0.2, 0.3, 0.6, 0.9}, // FADING roll
            {0.4, 0.6, 0.8, 1.2}  // INVISIBLE roll
    };

    private static final double[] DECAY_LEVEL = {0.0, 1.0 / 7.0, 1.0 / 3.0, 0.04};
    private static final double[] SPEED_LEVEL = {1.0, 1.2, 1.4, 1.6};

    private static final double INITIAL_COMBO_BONUS = 0.002;
    private static final double COMBO_BONUS_STEP    = 0.0005;
    private static final double MAX_COMBO_BONUS     = 0.004;

    private RollPhase rollPhase = RollPhase.NORMAL;
    private double grade = 0.0;
    private double rollGrade = 0.0;
    private double comboBonus = INITIAL_COMBO_BONUS;
    private int combo = 0;

    private MasterGrade currentGrade = MasterGrade.G9;
    private RollGrade currentRollGrade = RollGrade.MK;

    /* ========================== GRADE PROGRESSION ========================== */

    public void decay(int tier, RollPhase phase) {
        int tierIndex = Math.clamp(tier - 1, 0, DECAY_LEVEL.length - 1);
        double decayAmount = DECAY_LEVEL[tierIndex];

        if (phase == RollPhase.NORMAL) {
            double floor = getFloor(MasterGrade.values(), currentGrade);
            grade = Math.max(floor, grade - decayAmount);
        } else if (canAdvanceRollGrade()) {
            double floor = (rollGrade < RollGrade.MK.threshold())
                    ? 0.0
                    : getFloor(RollGrade.values(), currentRollGrade);
            rollGrade = Math.max(floor, rollGrade - decayAmount);
        }
    }

    public void calculate(int lines, SpinType spin, int tier, RollPhase phase) {
        if (lines <= 0) {
            combo = 0;
            return;
        }

        double earnedCombo = computeComboBonus(lines);
        if (lines >= 2) {
            combo++;
            comboBonus = Math.min(comboBonus + COMBO_BONUS_STEP, MAX_COMBO_BONUS);
            earnedCombo += comboBonus;
        }

        SpinType effectiveSpin = (spin != null) ? spin : SpinType.NONE;
        double spinBonus = switch (effectiveSpin) {
            case T_SPIN -> (phase == RollPhase.INVISIBLE) ? 0.04 : 0.02;
            case NONE   -> 0.0;
            default     -> (phase == RollPhase.INVISIBLE) ? 0.015 : 0.01;
        };

        int tierIndex = Math.clamp(tier - 1, 0, SPEED_LEVEL.length - 1);
        double multiplier = (phase == RollPhase.NORMAL) ? SPEED_LEVEL[tierIndex] : 1.0;
        int lineIndex = Math.clamp(lines - 1, 0, 3);
        double basePoints = LINE_CLEAR_VALUES[phase.ordinal()][lineIndex] + spinBonus;

        if (rollPhase != RollPhase.NORMAL) {
            if (canAdvanceRollGrade()) {
                rollGrade += basePoints + earnedCombo;
            }
        } else {
            grade += (basePoints * multiplier) + earnedCombo;
        }

        updateGrade(phase);
    }

    private double computeComboBonus(int lines) {
        if (lines >= 2 && combo > 1) {
            return comboBonus;
        } else {
            combo++;
            comboBonus = Math.min(comboBonus + COMBO_BONUS_STEP, MAX_COMBO_BONUS);
            return comboBonus;
        }
    }

    public void resetCombo() {
        combo = 0;
        comboBonus = INITIAL_COMBO_BONUS;
    }

    public void addGradeValue(double amount) {
        this.grade += amount;
        updateGrade(rollPhase);
    }

    public void addRollGrade(double amount) {
        if (canAdvanceRollGrade()) {
            this.rollGrade += amount;
            updateGrade(rollPhase);
        }
    }

    public void applyRegret() {
        MasterGrade previous = (currentGrade.ordinal() > 0)
                ? MasterGrade.values()[currentGrade.ordinal() - 1]
                : MasterGrade.G9;
        grade = getFloor(MasterGrade.values(), previous);
        currentGrade = previous;
    }

    public void setPhase(RollPhase phase, double overflow) {
        this.rollPhase = (phase != null) ? phase : RollPhase.NORMAL;
        if (this.rollPhase != RollPhase.NORMAL && currentGrade == MasterGrade.M) {
            this.rollGrade = Math.max(0.0, overflow);
        } else {
            this.rollGrade = 0.0;
        }
        this.currentRollGrade = RollGrade.MK;
        updateGrade(this.rollPhase);
    }

    private void updateGrade(RollPhase phase) {
        if (phase == RollPhase.NORMAL) {
            currentGrade = fromValue(MasterGrade.values(), grade);
        } else if (canAdvanceRollGrade()) {
            currentRollGrade = fromValue(RollGrade.values(), rollGrade);
            // Cap at MM for FADING roll, GM for INVISIBLE roll
            RollGrade cap = (phase == RollPhase.FADING) ? RollGrade.MM : RollGrade.GM;
            if (currentRollGrade.ordinal() > cap.ordinal()) {
                currentRollGrade = cap;
            }
        }
    }

    private boolean canAdvanceRollGrade() {
        return rollPhase != RollPhase.NORMAL && currentGrade == MasterGrade.M;
    }

    /* ========================== QUERIES & THRESHOLDS ========================== */

    public String getDisplayGrade() {
        return getActiveGrade().label();
    }

    public RollPhase.GradeScale getActiveGrade() {
        if (rollPhase == RollPhase.NORMAL || currentGrade != MasterGrade.M) {
            return currentGrade;
        }
        if (rollGrade < RollGrade.MK.threshold()) {
            return currentGrade; // Displays MasterGrade.M ("Master") from 0.0 to 1.199
        }
        return currentRollGrade; // Displays MK ("MasterK") at 1.2+
    }

    public double getCurrentFloor() {
        if (rollPhase == RollPhase.NORMAL || !canAdvanceRollGrade()) {
            return getFloor(MasterGrade.values(), currentGrade);
        }
        if (rollGrade < RollGrade.MK.threshold()) {
            return 0.0;
        }
        return getFloor(RollGrade.values(), currentRollGrade);
    }

    public double getProgressToNextGrade() {
        updateGrade(rollPhase);
        if (rollPhase == RollPhase.NORMAL || !canAdvanceRollGrade()) {
            double floor = getFloor(MasterGrade.values(), currentGrade);
            return Math.clamp((grade - floor) / currentGrade.threshold(), 0.0, 1.0);
        }
        if (rollGrade < RollGrade.MK.threshold()) {
            return Math.clamp(rollGrade / RollGrade.MK.threshold(), 0.0, 1.0);
        }
        double floor = getFloor(RollGrade.values(), currentRollGrade);
        return Math.clamp((rollGrade - floor) / currentRollGrade.threshold(), 0.0, 1.0);
    }

    public double getGradeOverflow() {
        if (currentGrade != MasterGrade.M) {
            return 0.0;
        }
        double floor = getFloor(MasterGrade.values(), MasterGrade.M);
        double ceiling = floor + MasterGrade.M.threshold();
        return Math.max(0.0, grade - ceiling);
    }

    public <T extends Enum<T> & RollPhase.GradeScale> double getFloor(T[] values, T target) {
        double cumulative = 0.0;
        for (T g : values) {
            if (g == target) return cumulative;
            cumulative += g.threshold();
        }
        return 0.0;
    }

    private <T extends Enum<T> & RollPhase.GradeScale> T fromValue(T[] values, double value) {
        double cumulative = 0.0;
        for (T g : values) {
            cumulative += g.threshold();
            if (value < cumulative) return g;
        }
        return values[values.length - 1];
    }

    public MasterGrade getCurrentGrade()      { return currentGrade; }
    public RollGrade getCurrentRollGrade()    { return currentRollGrade; }
    public RollPhase getRollPhase()           { return rollPhase; }
    public double getGrade()                  { return grade; }
    public double getGradeValue()             { return grade; }
    public double getRollGrade()              { return rollGrade; }
    public int getCombo()                     { return combo; }
}