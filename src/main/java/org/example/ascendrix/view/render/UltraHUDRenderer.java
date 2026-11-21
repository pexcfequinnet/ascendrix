package org.example.ascendrix.view.render;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.ascendrix.model.mode.ModeRuleset;
import org.example.ascendrix.model.mode.UltraRuleset;
import org.example.ascendrix.model.state.GameTimer;
import org.example.ascendrix.model.state.HUDState;

public class UltraHUDRenderer extends BaseHUDRenderer {

    private static final int BOARD_CENTER_X = 512;
    private static final int RIGHT_X        = 750;
    private static final int LEFT_LABEL_X   = 100;
    private static final int LEFT_VALUE_X   = 310;

    private static final Color ULTRA_ACCENT = Color.web("#FFA726");
    private static final Color URGENT_RED   = Color.web("#EF5350");

    @Override
    public void render(GraphicsContext gc, ModeRuleset ruleset, GameTimer timer, long now) {
        HUDState hud = ruleset.getHUDState();
        if (hud == null) return;

        int statsY = 550;
        int spacing = 38;

        // 1. Left Panel: Ultra Stats (Uncapped Lines & Score)
        gc.save();
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(ULTRA_ACCENT);
        gc.setFont(Font.font("System", FontWeight.BOLD, 28));
        gc.fillText("ULTRA", LEFT_VALUE_X, statsY);

        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 22));

        // Lines
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Lines:", LEFT_LABEL_X, statsY + spacing * 1.5);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(hud.getLinesCleared()), LEFT_VALUE_X, statsY + spacing * 1.5);

        // Score
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Score:", LEFT_LABEL_X, statsY + spacing * 2.5);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.GOLD);
        gc.fillText(String.format("%,d", hud.getScore()), LEFT_VALUE_X, statsY + spacing * 2.5);
        gc.restore();

        // 2. Right Panel: 2-Minute Countdown Clock
        long totalDurationMs = (ruleset instanceof UltraRuleset ultra)
                ? ultra.getDurationMs()
                : UltraRuleset.DEFAULT_DURATION_MS;
        long remainingMs = (timer != null)
                ? Math.max(0L, totalDurationMs - timer.getElapsedMs(now))
                : hud.getTimeMs();

        Color clockColor = Color.WHITE;
        if (remainingMs <= 10_000L) {
            clockColor = URGENT_RED;
        } else if (remainingMs <= 30_000L) {
            clockColor = ULTRA_ACCENT;
        }

        gc.save();
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("System", FontWeight.BOLD, 20));
        gc.fillText("TIME LEFT", RIGHT_X, statsY + spacing * 1.5);

        gc.setFill(clockColor);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 32));
        gc.fillText(formatTime(remainingMs), RIGHT_X, statsY + spacing * 2.5 + 10);
        gc.restore();

        // 3. Board-Centered Clear / Spin Callout
        if (hud.hasActiveClearCallout(now)) {
            String clearText = buildClearText(hud.getLastSpin(), hud.getLastLinesCleared());
            if (!clearText.isEmpty()) {
                gc.save();
                gc.setGlobalAlpha(getAlpha(hud.getClearDisplayUntilNs(), now));

                double noticeW = 180;
                double noticeH = 42;
                double noticeX = BOARD_CENTER_X - (noticeW / 2);
                double noticeY = 180;

                gc.setFill(Color.color(0, 0, 0, 0.75));
                gc.fillRoundRect(noticeX, noticeY, noticeW, noticeH, 8, 8);
                gc.setStroke(ULTRA_ACCENT);
                gc.setLineWidth(1.5);
                gc.strokeRoundRect(noticeX, noticeY, noticeW, noticeH, 8, 8);

                gc.setFill(Color.GOLD);
                gc.setFont(Font.font("System", FontWeight.BOLD, 18));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.CENTER);
                gc.fillText(clearText, BOARD_CENTER_X, noticeY + noticeH / 2);
                gc.restore();
            }
        }

        // 4. Tier-Colored Back-to-Back Streak
        int b2bStreak = hud.getB2bStreak();
        if (b2bStreak > 0) {
            gc.save();
            Color b2bColor = switch (UltraRuleset.getB2BTier(b2bStreak)) {
                case 0 -> Color.YELLOW;
                case 1 -> Color.ORANGE;
                case 2 -> Color.RED;
                case 3 -> Color.PURPLE;
                case 4 -> Color.CYAN;
                default -> Color.SILVER;
            };

            double b2bY = 630;
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(4.0);
            gc.strokeText("B2B x" + b2bStreak, LEFT_LABEL_X, b2bY);

            gc.setFill(b2bColor);
            gc.fillText("B2B x" + b2bStreak, LEFT_LABEL_X, b2bY);
            gc.restore();
        }

        // 5. Combo Counter
        int combo = hud.getCombo();
        if (combo > 1) {
            gc.save();
            Color comboColor = switch (combo) {
                case 1, 2 -> Color.YELLOW;
                case 3, 4 -> Color.ORANGE;
                case 5, 6 -> Color.RED;
                case 7, 8, 9 -> Color.PURPLE;
                default -> Color.CYAN;
            };

            double comboY = 675;
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 24));

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(4.0);
            gc.strokeText(combo + " COMBO", BOARD_CENTER_X, comboY);

            gc.setFill(comboColor);
            gc.fillText(combo + " COMBO", BOARD_CENTER_X, comboY);
            gc.restore();
        }
    }
}