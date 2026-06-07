package org.example.ascendrix.GameMode.Overdrive;

public class OverdriveGradeHandler {
    private OverdriveGradeTable currentGrade = OverdriveGradeTable.G1;
    private boolean rollCleared = false;

    public void onSectionPass() {
        currentGrade = currentGrade.next();
    }

    public void onRollCleared() {
        rollCleared = true;
    }
    public void onRegret() {
        currentGrade = currentGrade.ordinal() > 0
                ? OverdriveGradeTable.values()[currentGrade.ordinal() - 1]
                : OverdriveGradeTable.G1;
    }
    public OverdriveGradeTable getCurrentGrade() { return currentGrade; }
    public boolean isRollCleared()          { return rollCleared; }
    public boolean isMaxGrade()             { return currentGrade.isMax(); }
}
