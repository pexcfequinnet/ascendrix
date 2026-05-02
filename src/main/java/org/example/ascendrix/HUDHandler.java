package org.example.ascendrix;

public class HUDHandler{

    public int level;
    public int nextLevel;
    public String grade;
    public long time;

    public String clearText = "";
    public long displayUntil = 0;

    public boolean shouldDisplay(long now) {
        return now <= displayUntil;
    }

    public double getAlpha(long now) {
        return (displayUntil - now) / 800_000_000.0;
    }

    public String getClearText() {
        return clearText;
    }

    public void updateStats(int level, int nextLevel, String grade, long time) {
        this.level = level;
        this.nextLevel = nextLevel;
        this.grade = grade;
        this.time = time;
    }

    public void showClear(SpinType spin, int lines, long now) {
        if (lines == 0) return;

        clearText = buildText(spin, lines);
        displayUntil = now + 800_000_000;
    }

    // Spin Indicator
    public void showSpin(SpinType spin, int lines, long now) {
        if (spin == SpinType.NONE) return;

        clearText = buildText(spin, lines);
        displayUntil = now + 800_000_000; // 0.8s
    }

    private String buildText(SpinType spin, int lines) {
        if (spin == SpinType.NONE) {
            return switch (lines) {
                case 1 -> "SINGLE";
                case 2 -> "DOUBLE";
                case 3 -> "TRIPLE";
                case 4 -> "QUAD";
                default -> "";
            };
        }
        String base = switch (spin) {
            case T_SPIN -> "T-SPIN";
            case MINI_T_SPIN -> "MINI T-SPIN";
            case MINI_L_SPIN -> "MINI L-SPIN";
            case MINI_J_SPIN -> "MINI J-SPIN";
            case MINI_S_SPIN -> "MINI S-SPIN";
            case MINI_Z_SPIN -> "MINI Z-SPIN";
            case MINI_I_SPIN -> "MINI I-SPIN";
            default -> "";
        };

        String lineText = switch (lines) {
            case 1 -> " SINGLE";
            case 2 -> " DOUBLE";
            case 3 -> " TRIPLE";
            case 4 -> " QUAD";
            default -> "";
        };

        return base + lineText;
    }
}