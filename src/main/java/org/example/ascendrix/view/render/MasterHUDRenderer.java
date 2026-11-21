package org.example.ascendrix.view.render;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.ascendrix.model.mode.MasterRuleset;
import org.example.ascendrix.model.mode.ModeRuleset;
import org.example.ascendrix.model.state.GameTimer;
import org.example.ascendrix.model.state.HUDState;

public class MasterHUDRenderer extends BaseHUDRenderer {

    private static final int BOARD_X        = 342;
    private static final int BOARD_CENTER_X = 512;
    private static final int BOARD_BOTTOM_Y = 724;

    private static final int HUD_X          = 140;
    private static final int BAR_WIDTH      = 180;
    private static final int PANEL_CENTER_X = HUD_X + (BAR_WIDTH / 2); // X = 230

    @Override
    public void render(GraphicsContext gc, ModeRuleset ruleset, GameTimer timer, long now) {
        HUDState hud = ruleset.getHUDState();
        if (hud == null) return;

        // 1. Action Clear / Spin Notification
        if (hud.hasActiveClearCallout(now)) {
            String clearText = buildClearText(hud.getLastSpin(), hud.getLastLinesCleared());
            if (!clearText.isEmpty()) {
                gc.save();
                gc.setGlobalAlpha(getAlpha(hud.getClearDisplayUntilNs(), now));

                gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.CENTER);

                gc.setStroke(Color.BLACK);
                gc.setLineWidth(4.0);
                gc.strokeText(clearText, PANEL_CENTER_X, 200);

                gc.setFill(Color.GOLD);
                gc.fillText(clearText, PANEL_CENTER_X, 200);
                gc.restore();
            }
        }

        // 2. Combo Counter
        int combo = hud.getCombo();
        if (combo > 2) {
            gc.save();
            Color comboColor = switch (combo) {
                case 1, 2 -> Color.YELLOW;
                case 3, 4 -> Color.ORANGE;
                case 5, 6 -> Color.RED;
                case 7, 8, 9 -> Color.PURPLE;
                default -> Color.CYAN;
            };

            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3.0);
            gc.strokeText(combo + " COMBO", PANEL_CENTER_X, 240);

            gc.setFill(comboColor);
            gc.fillText(combo + " COMBO", PANEL_CENTER_X, 240);
            gc.restore();
        }

        // 3. Lower Left Panel: Time, Level, Grade
        int statsStartY = 450;
        long timeMs = (timer != null) ? timer.getElapsedMs(now) : hud.getTimeMs();

        gc.save();
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        gc.fillText("TIME", HUD_X, statsStartY);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        gc.fillText(formatTime(timeMs), HUD_X, statsStartY + 25);
        gc.restore();

        // Level Progress
        gc.save();
        int displayLevel = Math.min(hud.getLevel(), MasterRuleset.MAX_LEVEL);
        int sectionStart = (displayLevel / 100) * 100;
        int sectionEnd = Math.max(sectionStart + 1, hud.getNextLevel());
        double sectionPercent = (displayLevel >= MasterRuleset.MAX_LEVEL)
                ? 1.0
                : (double) (displayLevel - sectionStart) / 100.0;

        int levelY = statsStartY + 85;

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("LEVEL", HUD_X, levelY);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 22));
        gc.fillText(String.format("%03d", displayLevel), HUD_X, levelY + 25);

        gc.setFill(Color.GRAY);
        gc.setFont(Font.font("Consolas", FontWeight.NORMAL, 16));
        gc.fillText(" / " + sectionEnd, HUD_X + 45, levelY + 25);

        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillRoundRect(HUD_X, levelY + 35, BAR_WIDTH, 8, 4, 4);
        if (sectionPercent > 0.0) {
            gc.setFill(hud.isRollActive() ? Color.PURPLE : Color.CYAN);
            gc.fillRoundRect(HUD_X, levelY + 35, BAR_WIDTH * Math.min(1.0, sectionPercent), 8, 4, 4);
        }
        gc.restore();

        // Grade & Internal Progress
        gc.save();
        String letterGrade = hud.getGrade();
        double progress = hud.getGradeProgress();

        int gradeY = levelY + 85;

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("GRADE", HUD_X, gradeY);

        gc.setFill(Color.ORANGE);
        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 32));
        gc.fillText(letterGrade, HUD_X, gradeY + 32);

        gc.setFill(Color.DARKGRAY);
        gc.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        gc.fillText(String.format("(%.1f%%)", progress * 100.0), HUD_X + 65, gradeY + 28);

        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillRoundRect(HUD_X, gradeY + 45, BAR_WIDTH, 8, 4, 4);
        if (progress > 0.0) {
            gc.setFill(Color.ORANGE);
            gc.fillRoundRect(HUD_X, gradeY + 45, BAR_WIDTH * progress, 8, 4, 4);
        }
        gc.restore();

        // 4. Bottom Board Banners: COOL! / REGRET
        final double bottomPanelHeight = 44.0;
        final double noticeBoxH = 32.0;
        final double noticeBoxY = BOARD_BOTTOM_Y + (bottomPanelHeight - noticeBoxH) / 2.0;
        final double boxWidth = 330.0;

        double coolAlpha = hud.getCoolAlpha();
        if (coolAlpha > 0.0) {
            gc.save();
            gc.setGlobalAlpha(coolAlpha);

            gc.setFill(Color.color(0.0, 0.5, 1.0, 0.85));
            gc.fillRoundRect(BOARD_X + 5, noticeBoxY, boxWidth, noticeBoxH, 6, 6);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("COOL!", BOARD_CENTER_X, noticeBoxY + noticeBoxH / 2.0);
            gc.restore();
        }

        double regretAlpha = hud.getRegretAlpha();
        if (regretAlpha > 0.0) {
            gc.save();
            gc.setGlobalAlpha(regretAlpha);

            gc.setFill(Color.color(1.0, 0.0, 0.0, 0.85));
            gc.fillRoundRect(BOARD_X + 5, noticeBoxY, boxWidth, noticeBoxH, 6, 6);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("REGRET", BOARD_CENTER_X, noticeBoxY + noticeBoxH / 2.0);
            gc.restore();
        }
    }
}