package org.example.ascendrix.GameMode.Sprint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.example.ascendrix.MainGame.Renderer.HUDHelper;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.GameMode.GameModeHandler;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoQueue;

public class SprintModeHandler implements GameModeHandler {
    private final SprintRuleset ruleset;
    private final int targetLines;
    private int linesCleared = 0;
    private boolean b2bActive = false;
    private int b2bStreak = 0;
    private long finalTimeMs = 0;
    public SprintModeHandler(int targetLines) {
        this.ruleset = SprintRuleset.create();
        this.targetLines = targetLines;
    }
    private final HUDHelper hud = new HUDHelper();

    @Override public boolean supportsIRS() { return false; }
    @Override public boolean supportsIHS() { return false; }
    @Override public void setPerfectClearFlag(boolean flag){}

    @Override
    public boolean supportsPerfectClear() {return true;}

    @Override
    public RulesetHandler getRuleset() { return ruleset; }

    @Override
    public void onLinesCleared(int lines, SpinType spin, int pendingDropRows, TetrominoQueue.DropType pendingDropType, GameEngine game){
        linesCleared += lines;
        boolean isB2B = lines == 4 || spin != SpinType.NONE;

        if (isB2B && b2bActive) {
            b2bStreak++;
        } else {
            b2bStreak = 0;
        }
        b2bActive = isB2B; // update after the check

        if (linesCleared >= targetLines) {
            this.finalTimeMs = game.timer.getElapsedMs();
            game.clearGame();
        }
        hud.showClear(spin, lines, System.nanoTime());
    }

    @Override
    public void renderHUD(GraphicsContext g, GameTimer timer, long now) {
        // -----------------------------------------------------------------
        // 1. RENDER ACTION NOTIFICATIONS
        // -----------------------------------------------------------------
        g.save();
        if (hud.shouldDisplay(now)) {
            double alpha = hud.getAlpha(now);
            g.setGlobalAlpha(alpha);

            g.setFill(Color.color(0, 0, 0, 0.75));
            g.fillRoundRect(140, 150, 110, 50, 10, 10);

            g.setFill(Color.ORANGE);
            g.setFont(Font.font("System", FontWeight.BOLD, 16));
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(hud.getClearText(), 195, 180);
        }
        g.restore();

        // -----------------------------------------------------------------
        // 2. RENDER BACK-TO-BACK (B2B) STREAK
        // -----------------------------------------------------------------
        if (b2bActive && b2bStreak > 0) {
            g.save();
            g.setFill(Color.YELLOW);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18));
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText("B2B x" + b2bStreak, 195, 230);
            g.restore();
        }


        // -----------------------------------------------------------------
        // 3. RENDER SPRINT STATS
        // -----------------------------------------------------------------
        g.save();
        final int LEFT_X = 240;
        int startY = 400;
        int spacing = 30;

        g.setTextAlign(TextAlignment.RIGHT);

        g.setFill(Color.LIGHTGREEN);
        g.setFont(Font.font("System", FontWeight.BOLD, 22));
        g.fillText("SPRINT", LEFT_X, startY);

        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        g.fillText("Lines: ", LEFT_X - 80, startY + spacing);

        g.setFill(Color.WHITE);
        g.fillText(linesCleared + " / " + targetLines, LEFT_X, startY + spacing);

        double progress = Math.min(1.0, (double) linesCleared / targetLines);
        int barWidth = 120;
        g.setFill(Color.rgb(40, 40, 40));
        g.fillRoundRect(LEFT_X - barWidth, startY + spacing + 10, barWidth, 6, 3, 3);
        if (progress > 0) {
            g.setFill(Color.LIGHTGREEN);
            g.fillRoundRect(LEFT_X - barWidth, startY + spacing + 10, barWidth * progress, 6, 3, 3);
        }
        g.restore();

        // -----------------------------------------------------------------
        // 4. RENDER SPRINT TIMER
        // -----------------------------------------------------------------
        g.save();
        final int RIGHT_X = 560;
        long time = (timer != null) ? timer.getElapsedMs() : 0;
        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 16));
        g.fillText("TIME", RIGHT_X, startY);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 28)); // Đổi cỡ chữ lên 28
        g.fillText(GameTimer.formatTime(time), RIGHT_X, startY + spacing + 5);
        g.restore();
    }
    @Override
    public long getSortValue() {
        return this.finalTimeMs;
    }

    @Override
    public String getDisplayValue() {
        return GameTimer.formatTime(this.finalTimeMs);
    }
}