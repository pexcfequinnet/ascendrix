package org.example.ascendrix.view.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.ascendrix.model.mechanics.rotation.SpinType;
import org.example.ascendrix.model.state.HUDState;

public abstract class BaseHUDRenderer implements ModeHUDRenderer {

    private static final double FADE_WINDOW_NS = 800_000_000.0;
    protected static final int LEFT_CENTER_X = 171;

    protected void renderClearNotification(GraphicsContext gc, HUDState hud, long now) {
        if (!hud.hasActiveClearCallout(now)) return;

        String clearText = buildClearText(hud.getLastSpin(), hud.getLastLinesCleared());
        if (clearText.isEmpty()) return;

        gc.save();
        gc.setGlobalAlpha(getAlpha(hud.getClearDisplayUntilNs(), now));

        int boxWidth = 140;
        int boxHeight = 50;
        int boxX = LEFT_CENTER_X - (boxWidth / 2);
        int boxY = 280;

        gc.setFill(Color.color(0, 0, 0, 0.75));
        gc.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

        gc.setFill(hud.getLastSpin() != SpinType.NONE ? Color.MAGENTA : Color.ORANGE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(clearText, LEFT_CENTER_X, boxY + 32);
        gc.restore();
    }

    protected void renderB2BStreak(GraphicsContext gc, HUDState hud) {
        if (hud.getB2bStreak() <= 0) return;

        gc.save();
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("B2B x" + hud.getB2bStreak(), LEFT_CENTER_X, 360);
        gc.restore();
    }

    protected double getAlpha(long displayUntilNs, long nowNs) {
        double raw = (displayUntilNs - nowNs) / FADE_WINDOW_NS;
        return Math.max(0.0, Math.min(1.0, raw));
    }

    protected String buildClearText(SpinType spin, int lines) {
        if (spin == null || spin == SpinType.NONE) {
            return switch (lines) {
                case 1 -> "\nSINGLE";
                case 2 -> "\nDOUBLE";
                case 3 -> "\nTRIPLE";
                case 4 -> "\nQUAD";
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
            case 1 -> "\nSINGLE";
            case 2 -> "\nDOUBLE";
            case 3 -> "\nTRIPLE";
            case 4 -> "\nQUAD";
            default -> "";
        };

        return base + lineText;
    }

    public static String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long centiseconds = (ms % 1000) / 10;

        return String.format("%02d:%02d.%02d", minutes, seconds, centiseconds);
    }
}