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
            game.clearGame();
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
        // CÁC TRỤC TỌA ĐỘ VÀNG (Chuẩn màn hình 1024x768)
        // -----------------------------------------------------------------
        final int BOARD_CENTER_X = 512; // Tâm tuyệt đối của bảng game
        final int RIGHT_X = 750;        // Neo lề trái cho Panel phải

        // Căn chỉnh 2 cột hoàn hảo cho Panel trái
        final int LEFT_LABEL_X = 100;   // Mép trái của các nhãn (Level, Lines...)
        final int LEFT_VALUE_X = 310;   // Mép phải của các con số giá trị

        int statsY = 550;
        int spacing = 38; // Nới rộng khoảng cách dòng cho thoáng mắt

        // -----------------------------------------------------------------
        // 1. MARATHON STATS - BẢNG ĐIỂM HAI CỘT (ĐÃ PHÓNG TO CHỮ)
        // -----------------------------------------------------------------
        g.save();
        // Tiêu đề to hẳn lên cỡ 28
        g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Color.CYAN);
        g.setFont(Font.font("System", FontWeight.BOLD, 28));
        g.fillText("MARATHON", LEFT_VALUE_X, statsY);

        // Các thông số tăng từ 18 lên cỡ 22 cho rõ nét
        g.setFont(Font.font("Consolas", FontWeight.BOLD, 22));

        // --- Hàng 1: Level ---
        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.LIGHTGRAY);
        g.fillText("Level:", LEFT_LABEL_X, statsY + spacing * 1.5);

        g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Color.WHITE);
        g.fillText(String.valueOf(level), LEFT_VALUE_X, statsY + spacing * 1.5);

        // --- Hàng 2: Lines ---
        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.LIGHTGRAY);
        g.fillText("Lines:", LEFT_LABEL_X, statsY + spacing * 2.5);

        g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Color.WHITE);
        g.fillText(linesCleared + " / " + targetLines, LEFT_VALUE_X, statsY + spacing * 2.5);

        // --- Hàng 3: Score ---
        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.LIGHTGRAY);
        g.fillText("Score:", LEFT_LABEL_X, statsY + spacing * 3.5);

        g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Color.GOLD);
        g.fillText(String.valueOf(score), LEFT_VALUE_X, statsY + spacing * 3.5);
        g.restore();

        // -----------------------------------------------------------------
        // 2. TIME - ĐỐI XỨNG HÌNH HỌC VỚI BÊN TRÁI
        // -----------------------------------------------------------------
        g.save();
        long time = (timer != null) ? timer.getElapsedMs() : 0;

        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 20));
        g.fillText("TIME", RIGHT_X, statsY + spacing * 1.5);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", FontWeight.BOLD, 32));
        g.fillText(GameTimer.formatTime(time), RIGHT_X, statsY + spacing * 2.5 + 10);
        g.restore();


        // =================================================================
        // KHU VỰC HIỆN THỊ TRÊN BOARD (CHỈ XUẤT HIỆN KHI CÓ EVENT)
        // =================================================================

        // -----------------------------------------------------------------
        // 3. ACTION NOTIFICATIONS - (1/3 Phía trên bảng game)
        // -----------------------------------------------------------------
        if (hud.shouldDisplay(now)) {
            g.save();
            double alpha = hud.getAlpha(now);
            g.setGlobalAlpha(alpha);

            double noticeW = 180;
            double noticeH = 42;
            double noticeX = BOARD_CENTER_X - (noticeW / 2);
            double noticeY = 180;

            g.setFill(Color.color(0, 0, 0, 0.75));
            g.fillRoundRect(noticeX, noticeY, noticeW, noticeH, 8, 8);
            g.setStroke(Color.GOLD);
            g.setLineWidth(1.5);
            g.strokeRoundRect(noticeX, noticeY, noticeW, noticeH, 8, 8);

            g.setFill(Color.GOLD);
            g.setFont(Font.font("System", FontWeight.BOLD, 18));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText(hud.getClearText(), BOARD_CENTER_X, noticeY + noticeH / 2);
            g.restore();
        }

        // -----------------------------------------------------------------
        // 4. BACK-TO-BACK STREAK - (Cận đáy bảng game)
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

            double b2bY = 630;
            g.setTextAlign(TextAlignment.CENTER);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));

            g.setStroke(Color.BLACK);
            g.setLineWidth(4.0);
            g.strokeText("B2B x" + b2bStreak, BOARD_CENTER_X, b2bY);

            g.setFill(b2bColor);
            g.fillText("B2B x" + b2bStreak, BOARD_CENTER_X, b2bY);
            g.restore();
        }

        // -----------------------------------------------------------------
        // 5. COMBO COUNTER - (Sát đáy bảng game)
        // -----------------------------------------------------------------
        if (combo > 1) {
            g.save();
            Color comboColor = switch (combo) {
                case 1, 2 -> Color.YELLOW;
                case 3, 4 -> Color.ORANGE;
                case 5, 6 -> Color.RED;
                case 7, 8, 9 -> Color.PURPLE;
                default -> Color.CYAN;
            };

            double comboY = 675;
            g.setTextAlign(TextAlignment.CENTER);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 24));

            g.setStroke(Color.BLACK);
            g.setLineWidth(4.0);
            g.strokeText(combo + " COMBO", BOARD_CENTER_X, comboY);

            g.setFill(comboColor);
            g.fillText(combo + " COMBO", BOARD_CENTER_X, comboY);
            g.restore();
        }
    }
    @Override
    public long getSortValue() {
        return this.score;
    }

    @Override
    public String getDisplayValue() {
        return String.format("%,d", this.score);
    }
}