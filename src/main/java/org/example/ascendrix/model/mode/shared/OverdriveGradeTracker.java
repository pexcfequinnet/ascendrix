package org.example.ascendrix.model.mode.shared;

public class OverdriveGradeTracker {

    private OverdriveGrade currentGrade = OverdriveGrade.G1;
    private boolean rollCleared = false;

    public void onSectionPass() {
        currentGrade = currentGrade.next();
    }

    public void onRollCleared() {
        rollCleared = true;
        if (currentGrade == OverdriveGrade.S15) {
            currentGrade = OverdriveGrade.S15_PLUS;
        }
    }

    public void onRegret() {
        OverdriveGrade[] grades = OverdriveGrade.values();
        currentGrade = (currentGrade.ordinal() > 0)
                ? grades[currentGrade.ordinal() - 1]
                : OverdriveGrade.G1;
    }

    public void reset() {
        currentGrade = OverdriveGrade.G1;
        rollCleared = false;
    }

    public String getDisplayGrade() {
        return currentGrade.label;
    }

    public OverdriveGrade getCurrentGrade() {
        return currentGrade;
    }

    public int getSortRank() {
        return currentGrade.ordinal();
    }

    public boolean isRollCleared() {
        return rollCleared;
    }

    public boolean isMaxGrade() {
        return currentGrade.isMax();
    }
}