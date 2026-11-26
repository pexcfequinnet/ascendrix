package org.example.ascendrix.view.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.ascendrix.model.mode.ModeRuleset;
import org.example.ascendrix.model.mode.SprintRuleset;
import org.example.ascendrix.model.state.GameTimer;
import org.example.ascendrix.model.state.HUDState;

import java.util.Locale;

public class SprintHUDRenderer extends BaseHUDRenderer {

    private static final int STATS_ANCHOR_X = 300;
    private static final int RIGHT_X = 750;
    private static final int START_Y = 560;
    private static final int SPACING = 35;

    @Override
    public void render(GraphicsContext gc, ModeRuleset ruleset, GameTimer timer, long now) {
        HUDState hud = ruleset.getHUDState();
        if (hud == null) return;

        // Shared callouts
        renderClearNotification(gc, hud, now);
        renderB2BStreak(gc, hud);

        // Sprint left panel: Lines + Progress Bar
        int cleared = hud.getLevel();
        int target = Math.max(1, hud.getNextLevel());

        gc.save();
        gc.setTextAlign(TextAlignment.RIGHT);

        gc.setFill(Color.LIGHTGREEN);
        gc.setFont(Font.font("System", FontWeight.BOLD, 26));
        gc.fillText("SPRINT", STATS_ANCHOR_X, START_Y);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        gc.fillText("Lines: ", STATS_ANCHOR_X - 100, START_Y + SPACING);

        gc.setFill(Color.WHITE);
        gc.fillText(cleared + " / " + target, STATS_ANCHOR_X, START_Y + SPACING);

        double progress = Math.min(1.0, (double) cleared / target);
        int barWidth = 160;
        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillRoundRect(STATS_ANCHOR_X - barWidth, START_Y + SPACING + 15, barWidth, 8, 4, 4);
        if (progress > 0) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRoundRect(STATS_ANCHOR_X - barWidth, START_Y + SPACING + 15, barWidth * progress, 8, 4, 4);
        }
        gc.restore();

        // Sprint right panel: Elapsed Clock + PPS
        long elapsedMs = (timer != null) ? timer.getElapsedMs(now) : hud.getTimeMs();
        int piecesPlaced = (ruleset instanceof SprintRuleset sprint) ? sprint.getPiecesPlaced() : 0;
        double pps = (elapsedMs > 0L) ? (piecesPlaced * 1000.0) / elapsedMs : 0.0;

        gc.save();
        gc.setTextAlign(TextAlignment.LEFT);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("System", FontWeight.BOLD, 22));
        gc.fillText("TIME", RIGHT_X, START_Y);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 36));
        gc.fillText(formatTime(elapsedMs), RIGHT_X, START_Y + SPACING + 10);

        int ppsY = START_Y + SPACING + 55;
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("System", FontWeight.BOLD, 18));
        gc.fillText("PPS", RIGHT_X, ppsY);

        gc.setFill(Color.web("#00C8C8"));
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 28));
        gc.fillText(String.format(Locale.US, "%.2f", pps), RIGHT_X, ppsY + 32);

        gc.restore();
    }
}