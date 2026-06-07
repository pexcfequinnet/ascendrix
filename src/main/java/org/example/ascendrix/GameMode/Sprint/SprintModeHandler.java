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

    public void renderHUD(GraphicsContext g, GameTimer timer, long now) {
        // Tâm của Panel Trái (Rộng 342px) -> Center = 171
        final int LEFT_CENTER_X = 171;

        // -----------------------------------------------------------------
        // 1. RENDER ACTION NOTIFICATIONS (VD: TETRIS, T-SPIN...)
        // -----------------------------------------------------------------
        g.save();
        if (hud.shouldDisplay(now)) {
            double alpha = hud.getAlpha(now);
            g.setGlobalAlpha(alpha);

            // Hộp thông báo đặt dưới chữ HOLD (Hold ở y=100)
            int boxWidth = 140; // Rộng hơn một chút để chứa chữ thoải mái
            int boxHeight = 50;
            int boxX = LEFT_CENTER_X - (boxWidth / 2); // Căn giữa hộp vào Panel trái
            int boxY = 280;

            g.setFill(Color.color(0, 0, 0, 0.75));
            g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);

            g.setFill(Color.ORANGE);
            g.setFont(Font.font("System", FontWeight.BOLD, 18)); // Font to lên 18
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(hud.getClearText(), LEFT_CENTER_X, boxY + 32);
        }
        g.restore();

        // -----------------------------------------------------------------
        // 2. RENDER BACK-TO-BACK (B2B) STREAK
        // -----------------------------------------------------------------
        if (b2bActive && b2bStreak > 0) {
            g.save();
            g.setFill(Color.YELLOW);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20)); // Font to lên 20
            g.setTextAlign(TextAlignment.CENTER);
            // Đặt ngay dưới hộp thông báo
            g.fillText("B2B x" + b2bStreak, LEFT_CENTER_X, 360);
            g.restore();
        }

        // -----------------------------------------------------------------
        // 3. RENDER SPRINT STATS (Bên trái, dưới B2B)
        // -----------------------------------------------------------------
        g.save();
        // Neo vào lề sát với bảng Game (Bảng game bắt đầu ở x=342)
        final int STATS_ANCHOR_X = 300;
        int startY = 600; // Đẩy sâu xuống dưới
        int spacing = 35; // Nới lỏng khoảng cách dòng

        g.setTextAlign(TextAlignment.RIGHT);

        g.setFill(Color.LIGHTGREEN);
        g.setFont(Font.font("System", FontWeight.BOLD, 26)); // To ra 26
        g.fillText("SPRINT", STATS_ANCHOR_X, startY);

        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        g.fillText("Lines: ", STATS_ANCHOR_X - 100, startY + spacing);

        g.setFill(Color.WHITE);
        g.fillText(linesCleared + " / " + targetLines, STATS_ANCHOR_X, startY + spacing);

        // Thanh tiến trình (Progress bar)
        double progress = Math.min(1.0, (double) linesCleared / targetLines);
        int barWidth = 160; // Dài ra 160px cho đẹp
        g.setFill(Color.rgb(40, 40, 40));
        g.fillRoundRect(STATS_ANCHOR_X - barWidth, startY + spacing + 15, barWidth, 8, 4, 4);
        if (progress > 0) {
            g.setFill(Color.LIGHTGREEN);
            g.fillRoundRect(STATS_ANCHOR_X - barWidth, startY + spacing + 15, barWidth * progress, 8, 4, 4);
        }
        g.restore();

        // -----------------------------------------------------------------
        // 4. RENDER SPRINT TIMER (Bên phải)
        // -----------------------------------------------------------------
        g.save();
        // Bảng game kết thúc ở x=682, cách ra một chút thì x=750 là đẹp
        final int RIGHT_X = 750;
        long time = (timer != null) ? timer.getElapsedMs() : 0;

        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 22)); // Chữ "TIME" to ra 22
        g.fillText("TIME", RIGHT_X, startY); // Cùng độ cao (Y) với chữ "SPRINT" bên kia

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 36)); // Đồng hồ siêu to 36
        g.fillText(GameTimer.formatTime(time), RIGHT_X, startY + spacing + 10);
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