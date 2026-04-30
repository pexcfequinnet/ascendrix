package org.example.ascendrix;

import javafx.scene.canvas.Canvas;

public class HUDHandler{

    public int level;
    public int nextLevel;
    public String grade;
    public long time;

    public String spinText = "";
    public long displayUntil = 0;

    public boolean shouldDisplay(long now) {
        return now <= displayUntil;
    }

    public double getAlpha(long now) {
        return (displayUntil - now) / 800_000_000.0;
    }

    public String getSpinText() {
        return spinText;
    }

    public void updateStats(int level, int nextLevel, String grade, long time) {
        this.level = level;
        this.nextLevel = nextLevel;
        this.grade = grade;
        this.time = time;
    }


    // Spin Indicator
    public void showSpin(SpinType spin, int lines, long now) {
        if (spin == SpinType.NONE) return;

        spinText = buildText(spin, lines);
        displayUntil = now + 800_000_000; // 0.8s
    }

    private String buildText(SpinType spin, int lines) {
        String base = switch (spin) {
            case T_SPIN -> "T-SPIN";
            case MINI_T_SPIN -> "MINI T-SPIN";
            case L_SPIN -> "L-SPIN";
            case J_SPIN -> "J-SPIN";
            case S_SPIN -> "S-SPIN";
            case Z_SPIN -> "Z-SPIN";
            case I_SPIN -> "I-SPIN";
            default -> "";
        };

        String lineText = switch (lines) {
            case 1 -> " SINGLE";
            case 2 -> " DOUBLE";
            case 3 -> " TRIPLE";
            default -> "";
        };

        return base + lineText;
    }


}