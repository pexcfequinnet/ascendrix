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
    @Override
    public boolean isRollActive() {
        return rollTriggered;
    }

    private void checkRollTransition() {
        if (rollTriggered || level < 1500) return;
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
        if (level >= 1000 && elapsed > TIME_LIMIT_1000) {
            timeLimitTriggered = true;
            game.clearGame();
        } else if (level >= 500 && elapsed > TIME_LIMIT_500) {
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
        // -----------------------------------------------------------------
        // TỌA ĐỘ VÀNG (Chuẩn 1024x768)
        // -----------------------------------------------------------------
        final int BOARD_X = 342;           // Điểm bắt đầu của bảng game
        final int BOARD_CENTER_X = 512;    // Tâm bảng
        final int BOARD_BOTTOM_Y = 724;    // Đáy bảng (cho event Regret)

        // Đẩy HUD sát mép trái bảng game
        final int HUD_X = 140;
        final int BAR_WIDTH = 180;
        final int LEFT_CENTER_X = HUD_X + (BAR_WIDTH / 2); // Tâm của phần hiển thị Action (X = 230)

        // =================================================================
        // NỬA TRÊN PANEL TRÁI: ACTION NOTIFICATIONS (TỐI GIẢN)
        // =================================================================
        if (hud.shouldDisplay(now)) {
            g.save();
            g.setGlobalAlpha(hud.getAlpha(now));

            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);

            // Bỏ hộp nền che khuất, dùng viền chữ đen nổi bật
            g.setStroke(Color.BLACK);
            g.setLineWidth(4.0);
            g.strokeText(hud.getClearText(), LEFT_CENTER_X, 200);

            g.setFill(Color.ORANGE);
            g.fillText(hud.getClearText(), LEFT_CENTER_X, 200);
            g.restore();
        }

        // =================================================================
        // NỬA DƯỚI PANEL TRÁI: THỐNG KÊ (Nằm sát chân bảng trái)
        // =================================================================
        int statsStartY = 450; // Hạ cụm UI xuống nửa dưới

        // -----------------------------------------------------------------
        // RENDER TIME
        // -----------------------------------------------------------------
        if (activeTimer != null) {
            this.timer = activeTimer;
        }

        long timeMs = (timer != null) ? timer.getElapsedMs() : 0;
        g.save();
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        g.fillText("TIME", HUD_X, statsStartY);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        g.fillText(GameTimer.formatTime(timeMs), HUD_X, statsStartY + 25);
        g.restore();

        // -----------------------------------------------------------------
        // RENDER LEVEL PROGRESS (Hỗ trợ 4 chữ số)
        // -----------------------------------------------------------------
        g.save();
        int displayLevel = Math.min(level, 1500);
        int sectionStart = (displayLevel / 100) * 100;
        int sectionEnd   = Math.min(sectionStart + 100, 1500);
        double sectionPercent = (displayLevel == 1500) ? 1.0 : (double)(displayLevel - sectionStart) / 100.0;

        int levelY = statsStartY + 85;

        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 14));
        g.fillText("LEVEL", HUD_X, levelY);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", FontWeight.BOLD, 22));

        String levelStr = displayLevel >= 1000
                ? String.format("%04d", displayLevel)
                : String.format("%03d", displayLevel);
        g.fillText(levelStr, HUD_X, levelY + 25);

        g.setFill(Color.GRAY);
        g.setFont(Font.font("Consolas", FontWeight.NORMAL, 16));
        // Dịch chữ " / 1500" sang phải thêm chút để né số level 4 chữ số
        int offset = displayLevel >= 1000 ? 55 : 45;
        g.fillText(" / " + sectionEnd, HUD_X + offset, levelY + 25);

        g.setFill(Color.rgb(40, 40, 40));
        g.fillRoundRect(HUD_X, levelY + 35, BAR_WIDTH, 8, 4, 4);
        if (sectionPercent > 0) {
            g.setFill(rollTriggered ? Color.GOLD : Color.CYAN);
            g.fillRoundRect(HUD_X, levelY + 35, BAR_WIDTH * sectionPercent, 8, 4, 4);
        }
        g.restore();

        // -----------------------------------------------------------------
        // RENDER GRADE
        // -----------------------------------------------------------------
        g.save();
        int gradeY = levelY + 85;

        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 14));
        g.fillText("GRADE", HUD_X, gradeY);

        g.setFill(gradeHandler.isRollCleared() ? Color.ORANGE : Color.WHITE);
        g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 32));
        g.fillText(gradeHandler.getCurrentGrade().label, HUD_X, gradeY + 32);
        g.restore();

        // =================================================================
        // KHU VỰC TRÊN BẢNG GAME (CHỈ COOL / REGRET)
        // =================================================================

        final double BOTTOM_PANEL_HEIGHT = 44;
        final double NOTICE_BOX_H = 32;
        final double NOTICE_BOX_Y = BOARD_BOTTOM_Y + (BOTTOM_PANEL_HEIGHT - NOTICE_BOX_H) / 2.0;

        double boxWidth = 330;
        // ---------------------------------------------
        // Regret display
        // ---------------------------------------------
        if (shouldDisplayRegret(now)) {
            g.save();
            g.setGlobalAlpha(getRegretAlpha(now));

            g.setFill(Color.color(1.0, 0, 0, 0.85));
            g.fillRoundRect(BOARD_X + 5, NOTICE_BOX_Y, boxWidth, NOTICE_BOX_H, 6, 6);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText("REGRET", BOARD_CENTER_X, NOTICE_BOX_Y + NOTICE_BOX_H / 2.0);
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

