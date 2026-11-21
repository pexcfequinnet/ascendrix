package org.example.ascendrix.model.state;

import org.example.ascendrix.model.mechanics.rotation.SpinType;

public class HUDState {
    private static final long CLEAR_DISPLAY_DURATION_NS = 1_000_000_000L;

    private int level;
    private int nextLevel;
    private String grade = "";
    private long timeMs;

    private SpinType lastSpin = SpinType.NONE;
    private int lastLinesCleared = 0;
    private int linesCleared = 0;
    private long score = 0L;
    private int combo = 0;
    private int b2bStreak = 0;
    private long clearDisplayUntilNs = 0L;
    private double gradeProgress = 0.0;
    private boolean rollActive = false;
    private double coolAlpha = 0.0;
    private double regretAlpha = 0.0;
    private boolean rollCleared = false;

    public void updateStats(int level, int nextLevel, String grade, long timeMs) {
        this.level = level;
        this.nextLevel = nextLevel;
        this.grade = (grade != null) ? grade : "";
        this.timeMs = timeMs;
    }

    public void recordClear(SpinType spin, int lines, long nowNs) {
        if (lines <= 0 && (spin == null || spin == SpinType.NONE)) {
            return;
        }
        this.lastSpin = (spin != null) ? spin : SpinType.NONE;
        this.lastLinesCleared = lines;
        this.clearDisplayUntilNs = nowNs + CLEAR_DISPLAY_DURATION_NS;
    }

    public void setB2bStreak(int b2bStreak) {
        this.b2bStreak = Math.max(0, b2bStreak);
    }

    public int getB2bStreak() {
        return b2bStreak;
    }

    public boolean hasActiveClearCallout(long nowNs) {
        return nowNs <= clearDisplayUntilNs && (lastLinesCleared > 0 || lastSpin != SpinType.NONE);
    }

    public void setGradeProgress(double gradeProgress) {
        this.gradeProgress = Math.clamp(gradeProgress, 0.0, 1.0);
    }
    public double getGradeProgress() { return gradeProgress; }

    public void setRollActive(boolean rollActive) { this.rollActive = rollActive; }
    public boolean isRollActive()                  { return rollActive; }

    public void setCoolAlpha(double coolAlpha)     { this.coolAlpha = Math.clamp(coolAlpha, 0.0, 1.0); }
    public double getCoolAlpha()                   { return coolAlpha; }

    public void setRegretAlpha(double regretAlpha) { this.regretAlpha = Math.clamp(regretAlpha, 0.0, 1.0); }
    public double getRegretAlpha()                 { return regretAlpha; }


    public void setRollCleared(boolean rollCleared) { this.rollCleared = rollCleared; }
    public boolean isRollCleared()                   { return rollCleared; }

    public int getLevel()                  { return level; }
    public int getNextLevel()              { return nextLevel; }
    public String getGrade()               { return grade; }
    public long getTimeMs()                { return timeMs; }
    public SpinType getLastSpin()          { return lastSpin; }
    public int getLastLinesCleared()       { return lastLinesCleared; }
    public long getClearDisplayUntilNs()   { return clearDisplayUntilNs; }
    public void setLinesCleared(int linesCleared) { this.linesCleared = Math.max(0, linesCleared); }
    public int getLinesCleared()                  { return linesCleared; }
    public void setScore(long score)              { this.score = Math.max(0L, score); }
    public long getScore()                        { return score; }
    public void setCombo(int combo)               { this.combo = Math.max(0, combo); }
    public int getCombo()                         { return combo; }
}