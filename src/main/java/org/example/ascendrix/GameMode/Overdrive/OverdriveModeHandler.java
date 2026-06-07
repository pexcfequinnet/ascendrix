package org.example.ascendrix.GameMode.Overdrive;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.example.ascendrix.GameMode.GameModeHandler;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.MainGame.Renderer.HUDHelper;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoQueue;

public class OverdriveModeHandler implements GameModeHandler {
    private final OverdriveRuleset ruleset;
    private GameTimer timer;
    private final OverdriveGradeHandler gradeHandler = new OverdriveGradeHandler();
    private final OverdriveSectionHandler sectionHandler = new OverdriveSectionHandler();
    private final HUDHelper hud = new HUDHelper();

    private int level = 0;
    private int lastSection = 0;
    private int sectionStartLevel = 0;
    private double sectionStartTime = 0;
    private final OverdriveGarbageHandler garbageHandler = new OverdriveGarbageHandler();
    // Time limits
    private static final long TIME_LIMIT_500  = 180_000L; // 3:00 in ms
    private static final long TIME_LIMIT_1000 = 360_000L; // 6:00 in ms
    private boolean timeLimitTriggered = false;

    // Roll
    private boolean rollTriggered = false;
    private long rollStartTime = -1;
    private static final long ROLL_DURATION_NS = 60_000_000_000L; // placeholder — needs confirmation

    // Regret display
    private long regretDisplayTime = -1;
    private static final long REGRET_DISPLAY_DURATION = 3_000_000_000L;

    public OverdriveModeHandler(GameTimer timer) {
        this.timer = timer;
        this.ruleset = OverdriveRuleset.create();
    }

    @Override
    public OverdriveRuleset getRuleset() { return ruleset; }

    @Override
    public void onPieceSpawned(GameEngine game) {
        if (rollTriggered) return;
        if (level < 1499 && level % 100 != 99)
            level++;
        checkSectionTransition(); // updateAll is called here on section change
        ruleset.updateARE(level);
        ruleset.updateLockDelay(level);
    }

    @Override
    public void onPiecePlaced(GameEngine game) {
        checkSectionTransition();
        ruleset.are.trigger(false, System.nanoTime());
    }

    @Override
    public void onLinesCleared(int lines, SpinType spin, int dropRows, TetrominoQueue.DropType dropType, GameEngine game) {
        hud.showClear(spin, lines, System.nanoTime());

        if (!rollTriggered) {
            switch (lines) {
                case 1 -> level = Math.min(level + 1, 1500);
                case 2 -> level = Math.min(level + 2, 1500);
                case 3 -> level = Math.min(level + 3, 1500);
                case 4 -> level = Math.min(level + 6, 1500);
            }
            checkSectionTransition();
            checkRollTransition();
        }

        ruleset.are.trigger(true, System.nanoTime());
    }

    private void checkRollTransition() {
        if (rollTriggered || level < 1499) return;
        rollTriggered = true;
        rollStartTime = -1;
        timer.pause();
        ruleset.updateAll(1500);
        ruleset.lockDelay.setLockResetLimit(8);
    }

    private void checkRollTimeout(long now, GameEngine game) {
        if (!rollTriggered) return;
        if (rollStartTime == -1) rollStartTime = now;
        if (now - rollStartTime >= ROLL_DURATION_NS) {
            gradeHandler.onRollCleared();
            game.clearGame();
        }
    }

    private void checkTimeLimit(GameEngine game) {
        if (rollTriggered || timeLimitTriggered) return;
        long elapsed = timer.getElapsedMs();
        if ((level >= 1000 && elapsed > TIME_LIMIT_1000) ||
                (level >= 500  && elapsed > TIME_LIMIT_500)) {
            timeLimitTriggered = true;
            game.clearGame();
        }
    }

    @Override
    public void update(long now, GameEngine game) {
        checkTimeLimit(game);
        checkRollTimeout(now, game);
        garbageHandler.update(now, level, game);
    }

    private void checkSectionTransition() {
        int currentSection = level / 100;
        if (currentSection <= lastSection) return;

        double sectionTime = timer.getElapsedSeconds() - sectionStartTime;
        int completedIdx = sectionHandler.getSectionIndex(lastSection * 100);

        if (completedIdx != -1) {
            OverdriveSectionHandler.SectionResult result =
                    sectionHandler.evaluate(completedIdx, sectionTime, gradeHandler);
            if (result == OverdriveSectionHandler.SectionResult.REGRET)
                triggerRegretDisplay(System.nanoTime());
        }

        lastSection = currentSection;
        sectionStartLevel = currentSection * 100;
        sectionStartTime = timer.getElapsedSeconds();
        ruleset.updateAll(level);
    }

    public void triggerRegretDisplay(long now) { regretDisplayTime = now; }

    public boolean shouldDisplayRegret(long now) {
        return regretDisplayTime != -1 && now - regretDisplayTime < REGRET_DISPLAY_DURATION;
    }

    public double getRegretAlpha(long now) {
        if (regretDisplayTime == -1) return 0;
        long elapsed = now - regretDisplayTime;
        if (elapsed > REGRET_DISPLAY_DURATION - 500_000_000L)
            return 1.0 - (double)(elapsed - (REGRET_DISPLAY_DURATION - 500_000_000L)) / 500_000_000L;
        return 1.0;
    }
    @Override
    public boolean isDecolorActive() {
        return level >= 1000;
    }
    @Override
    public void setPerfectClearFlag(boolean flag) {}

    @Override
    public void renderHUD(GraphicsContext g, GameTimer activeTimer, long now) {
        // Line clear notification
        g.save();
        if (hud.shouldDisplay(now)) {
            g.setGlobalAlpha(hud.getAlpha(now));
            g.setFill(Color.color(0, 0, 0, 0.75));
            g.fillRoundRect(140, 150, 110, 50, 10, 10);
            g.setFill(Color.ORANGE);
            g.setFont(Font.font("System", FontWeight.BOLD, 16));
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(hud.getClearText(), 195, 180);
        }
        g.restore();

        final int HUD_X = 30;
        final int BAR_WIDTH = 180;
        if (activeTimer != null) this.timer = activeTimer;

        // Timer
        long timeMs = (timer != null) ? timer.getElapsedMs() : 0;
        g.save();
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        g.fillText("TIME", HUD_X, 150);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        g.fillText(GameTimer.formatTime(timeMs), HUD_X, 175);
        g.restore();

        // Level
        g.save();
        int displayLevel = Math.min(level, 1499);
        int sectionStart = (displayLevel / 100) * 100;
        int sectionEnd   = Math.min(sectionStart + 100, 1499);
        double sectionPercent = (displayLevel == 1499) ? 1.0 : (double)(displayLevel - sectionStart) / 100.0;

        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 12));
        g.fillText("LEVEL", HUD_X, 220);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        g.fillText(String.format("%04d", displayLevel), HUD_X, 245);
        g.setFill(Color.GRAY);
        g.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        g.fillText(" / " + sectionEnd, HUD_X + 55, 245);

        g.setFill(Color.rgb(40, 40, 40));
        g.fillRoundRect(HUD_X, 255, BAR_WIDTH, 8, 4, 4);
        if (sectionPercent > 0) {
            g.setFill(rollTriggered ? Color.GOLD : Color.CYAN);
            g.fillRoundRect(HUD_X, 255, BAR_WIDTH * sectionPercent, 8, 4, 4);
        }
        g.restore();

        // Grade
        g.save();
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 12));
        g.fillText("GRADE", HUD_X, 305);
        g.setFill(gradeHandler.isRollCleared() ? Color.ORANGE : Color.WHITE);
        g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));
        g.fillText(gradeHandler.getCurrentGrade().label, HUD_X, 335);
        g.restore();

        // Regret display
        if (shouldDisplayRegret(now)) {
            g.save();
            g.setGlobalAlpha(getRegretAlpha(now));
            double boardX = 250, boardWidth = 300, boardBottomY = 650, bottomPanelHeight = 40;
            double boxWidth = boardWidth - 10, boxHeight = 28;
            double boxX = boardX + 5;
            double boxY = boardBottomY + (bottomPanelHeight - boxHeight) / 2.0;
            g.setFill(Color.color(1.0, 0, 0, 0.85));
            g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 6, 6);
            g.setFill(Color.WHITE);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText("REGRET", boardX + boardWidth / 2.0, boxY + boxHeight / 2.0);
            g.restore();
        }
    }
    @Override
    public String getDisplayValue() {
        return gradeHandler.getDisplayGrade();
    }

    ///// DEBUG: REMOVE IF DONE TESTING /////
    public void debugSetLevel(int targetLevel) {
        level = targetLevel;
        sectionStartLevel = (level / 100) * 100;
        lastSection = level / 100;
        sectionStartTime = timer.getElapsedSeconds();
        ruleset.updateAll(level);
        garbageHandler.reset();
    }
}

