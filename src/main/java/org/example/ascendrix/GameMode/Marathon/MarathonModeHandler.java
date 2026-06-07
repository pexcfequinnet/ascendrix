package org.example.ascendrix.GameMode.Marathon;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.TextAlignment;
import org.example.ascendrix.MainGame.Renderer.HUDHelper;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.GameMode.GameModeHandler;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoQueue;

public class MarathonModeHandler implements GameModeHandler {
    private final MarathonRuleset ruleset;
    private final HUDHelper hud = new HUDHelper();
    private final int targetLines;
    private int linesCleared = 0;
    private long score = 0;
    private int combo = 0;
    private boolean b2bActive = false;
    private int level = 1;
    private static final int[]    B2B_THRESHOLDS = { 7, 14, 22, 36, 54 };
    private static final double[] B2B_TIERS = {1.2, 1.4, 1.6, 1.8, 2.0};
    private static final double[] MINI_B2B_TIERS = {1.1, 1.2, 1.4, 1.6, 1.8};
    private int b2bStreak = 0;  // counts consecutive B2B actions
    private boolean perfectClear = false;


    @Override
    public void setPerfectClearFlag(boolean flag) {
        this.perfectClear = flag;
    }
    public MarathonModeHandler(int targetLines) {
        this.ruleset = MarathonRuleset.create();
        this.targetLines = targetLines;
    }

    @Override
    public boolean supportsPerfectClear() { return true; }

    @Override
    public MarathonRuleset getRuleset() {
        return ruleset;
    }
    private int getTier(int streak) {
        int tier = 0;
        for (int threshold : B2B_THRESHOLDS) {
            if (streak >= threshold) tier++;
            else break;
        }
        return tier;
    }


    @Override
    // Scoring system
    public void onLinesCleared(int lines, SpinType spin, int dropRows, TetrominoQueue.DropType dropType, GameEngine game) {
        // drop bonus always applies regardless of line clear
        int pc_bonus = 0;
        if (perfectClear)
            pc_bonus = 900;
        int spinBonus;
        score += dropBonus(dropRows, dropType);

        if (lines == 0) {
            // Render spin bonus with no line clear
            hud.showClear(spin, lines, System.nanoTime());
            combo = 0;
            return;
        }


        switch(spin){
            case T_SPIN -> spinBonus = 230;
            case NONE -> spinBonus = 0;
            default -> spinBonus = 115;
        }

        hud.showClear(spin, lines, System.nanoTime());
        linesCleared += lines;

        level = linesCleared / 10 + 1;

        ruleset.onLevelChanged(level);

        boolean isB2B = (lines == 4 || spin != SpinType.NONE);

        if (isB2B && b2bActive) {
            b2bStreak++;
        } else {
            b2bStreak = 0;
        }
        b2bActive = isB2B;

        // Spin bonus math
        int tier = getTier(b2bStreak);
        double b2bMultiplier = isB2B ? B2B_TIERS[tier] : 1.0;
        double spinMultiplier = switch (spin) {
            case T_SPIN -> B2B_TIERS[tier];
            case NONE -> 1.0;
            default -> MINI_B2B_TIERS[tier];
        };
        // Score formula
        double lineMultiplier = Math.pow((lines + pc_bonus) / 2.0, 1.25);
        double comboMultiplier = Math.log(combo + 1) / Math.log(10000) + 1.0;
        double levelMultiplier = Math.log(level) / Math.log(40) + 1.0;

        score += Math.round(1500.0 + (lineMultiplier + spinBonus) * spinMultiplier * b2bMultiplier * comboMultiplier * levelMultiplier);
        combo = Math.min(combo + 1, 25);

        if (linesCleared >= targetLines) {
            game.end();
        }
    }

    private long dropBonus(int rows, TetrominoQueue.DropType drop) {
        if (drop == TetrominoQueue.DropType.NONE)
            return 0;
        return (10L * rows) / 2;
    }
    @Override
    public void renderHUD(GraphicsContext g, GameTimer timer, long now) {
        // -----------------------------------------------------------------
        // 1. RENDER ACTION NOTIFICATIONS (Spins & Clears)
        // -----------------------------------------------------------------
        if (hud.shouldDisplay(now)) {
            g.save();
            double alpha = hud.getAlpha(now);
            g.setGlobalAlpha(alpha);

            double noticeW = 160;
            double noticeH = 40;
            double noticeX = 250 + (300 - noticeW) / 2; // = 320
            double noticeY = 180;

            g.setFill(Color.color(0, 0, 0, 0.75));
            g.fillRoundRect(noticeX, noticeY, noticeW, noticeH, 10, 10);
            g.setStroke(Color.ORANGE);
            g.setLineWidth(1.5);
            g.strokeRoundRect(noticeX, noticeY, noticeW, noticeH, 10, 10);

            g.setFill(Color.GOLD);
            g.setFont(Font.font("System", FontWeight.BOLD, 16));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText(hud.getClearText(), noticeX + noticeW / 2, noticeY + noticeH / 2);
            g.restore();
        }

        // -----------------------------------------------------------------
        // 2. RENDER BACK-TO-BACK (B2B) STREAK
        // -----------------------------------------------------------------
        if (b2bActive && b2bStreak > 0) {
            g.save();
            Color b2bColor = switch (getTier(b2bStreak)) {
                case 0 -> Color.YELLOW;
                case 1 -> Color.ORANGE;
                case 2 -> Color.RED;
                case 3 -> Color.PURPLE;
                case 4 -> Color.CYAN;
                default -> Color.SILVER;
            };

            g.setFill(b2bColor);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18));
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText("B2B x" + b2bStreak, 195, 230); // Đặt ngay dưới notification
            g.restore();
        }
// -----------------------------------------------------------------
        // 2.5. RENDER COMBO COUNTER
        // -----------------------------------------------------------------
        if (combo > 1) {
            g.save();

            // Hiệu ứng đổi màu: Combo càng cao màu càng cháy
            Color comboColor = switch (combo) {
                case 1, 2 -> Color.YELLOW;
                case 3, 4 -> Color.ORANGE;
                case 5, 6 -> Color.RED;
                case 7, 8, 9 -> Color.PURPLE;
                default -> Color.CYAN;
            };

            double comboX = 195;
            double comboY = 255;

            g.setTextAlign(TextAlignment.CENTER);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18)); // Size 18 bằng chuẩn với B2B

            // Vẽ đổ bóng (Shadow)
            g.setFill(Color.color(0, 0, 0, 0.6));
            g.fillText(combo + " COMBO", comboX + 2, comboY + 2);

            // Vẽ chữ chính
            g.setFill(comboColor);
            g.fillText(combo + " COMBO", comboX, comboY);

            g.restore();
        }
        // -----------------------------------------------------------------
        // 3. RENDER MARATHON STATS
        // -----------------------------------------------------------------
        g.save();
        final int LEFT_X = 240;
        int startY = 380;
        int spacing = 25;

        g.setTextAlign(TextAlignment.RIGHT);

        // Tên chế độ
        g.setFill(Color.CYAN);
        g.setFont(Font.font("System", FontWeight.BOLD, 18));
        g.fillText("MARATHON", LEFT_X, startY);
        g.setFill(Color.GRAY);
        g.setFont(Font.font("System", FontWeight.NORMAL, 12));
        g.fillText("(150 Lines)", LEFT_X, startY + 15);

        // Các thông số Level, Lines, Score
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 16));

        g.setFill(Color.LIGHTGRAY);
        g.fillText("Level: ", LEFT_X - 50, startY + spacing * 2);
        g.setFill(Color.WHITE);
        g.fillText(String.valueOf(level), LEFT_X, startY + spacing * 2);

        g.setFill(Color.LIGHTGRAY);
        g.fillText("Lines: ", LEFT_X - 80, startY + spacing * 3);
        g.setFill(Color.WHITE);
        g.fillText(linesCleared + " / " + targetLines, LEFT_X, startY + spacing * 3);

        g.setFill(Color.LIGHTGRAY);
        g.fillText("Score: ", LEFT_X - 80, startY + spacing * 4);
        g.setFill(Color.GOLD);
        g.fillText(String.valueOf(score), LEFT_X, startY + spacing * 4);
        g.restore();

        // -----------------------------------------------------------------
        // 4. RENDER TIME
        // -----------------------------------------------------------------
        g.save();
        final int RIGHT_X = 560;
        long time = (timer != null) ? timer.getElapsedMs() : 0; // Check null an toàn

        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 14));
        g.fillText("TIME", RIGHT_X, startY + spacing * 3);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        g.fillText(GameTimer.formatTime(time), RIGHT_X, startY + spacing * 4);
        g.restore();
    }
}