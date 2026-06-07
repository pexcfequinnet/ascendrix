package org.example.ascendrix.GameMode.Master;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.GameMode.GameModeHandler;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.MainGame.Renderer.BoardRenderContext;
import org.example.ascendrix.MainGame.Renderer.HUDHelper;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoQueue;
import org.example.ascendrix.Tetromino.TetrominoType;

public class MasterModeHandler implements GameModeHandler {
    private final MasterRuleset ruleset;
    private GameTimer timer;
    private final MasterGradeHandler gradeHandler = new MasterGradeHandler();
    private final MasterSectionHandler sectionHandler = new MasterSectionHandler();
    private final HUDHelper hud = new HUDHelper();
    private int currentSpdLv = 1;
    private int level = 0;
    private int lastSection = 0;
    private MasterRollPhase masterRollPhase = MasterRollPhase.NORMAL;
    private static final double[] SPEED_LEVEL = {1, 1.2, 1.4, 1.6};
    private int speedLevelStartLevel = 0;
    private int sectionStartLevel = 0;
    private boolean gravityAtFloor = false;  // true = locked at minimum
    private boolean gravityAtCeiling = false; // true = locked at maximum
    // Cool/Regret Display Handling
    private int lastEvaluatedSection = -1;
    private long coolDisplayTime = -1;
    private static final long COOL_DISPLAY_DURATION = 3_000_000_000L;
    private long regretDisplayTime = -1;
    private static final long REGRET_DISPLAY_DURATION = 3_000_000_000L;

    // Roll
    public final FadeMap fadeMap = new FadeMap();
    private long rollStartTime = -1;
    private boolean rollTriggered = false;
    private static final long ROLL_DURATION_NS   = 60_000_000_000L;

    // Cool display
    public void triggerCoolDisplay(long now) {
        coolDisplayTime = now;
    }

    public boolean shouldDisplayCool(long now) {
        return coolDisplayTime != -1 && now - coolDisplayTime < COOL_DISPLAY_DURATION;
    }

    public double getCoolAlpha(long now) {
        if (coolDisplayTime == -1) return 0;
        long elapsed = now - coolDisplayTime;
        if (elapsed > COOL_DISPLAY_DURATION - 500_000_000L)
            return 1.0 - (double)(elapsed - (COOL_DISPLAY_DURATION - 500_000_000L)) / 500_000_000L;
        return 1.0;
    }
    // Regret display
    public void triggerRegretDisplay(long now) {
        regretDisplayTime = now;
    }

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
    public void onPieceSpawned(GameEngine game) {
        int sectionCap = (level >= 900) ? 98 : 99;
        if (level < 999 && level % 100 != sectionCap)
            level++;
        checkSectionTransition();
        ruleset.updateGravity(currentSpdLv, level - sectionStartLevel);
        ruleset.updateLockDelay(getLockDelayForSpeedLevel(), 15);
        ruleset.are.trigger(false, System.nanoTime());
    }
    @Override
    public boolean isRollActive() {
        return rollTriggered;
    }
    @Override
    public void update(long now, GameEngine game) {
        checkRollTimeout(now, game);
    }

    private void checkRollTimeout(long now, GameEngine game) {
        if (!rollTriggered || masterRollPhase == MasterRollPhase.NORMAL) return;
        System.out.println("rollTimeout: elapsed=" + (now - rollStartTime) / 1_000_000_000.0 + "s limit=" + ROLL_DURATION_NS / 1_000_000_000.0 + "s");
        if (now - rollStartTime >= ROLL_DURATION_NS)
            onRollComplete(game);
    }

    @Override
    public void setPerfectClearFlag(boolean flag) {}
    public MasterModeHandler(GameTimer timer) {
        this.timer = timer;
        this.ruleset = MasterRuleset.create();
    }

    @Override
    public MasterRuleset getRuleset() {
        return ruleset;
    }
    private int getTier(int level) {
        int tier = 0;
        for (double threshold : SPEED_LEVEL) {
            if (level >= threshold) tier++;
            else break;
        }
        return tier;
    }
    @Override
    public void onPiecePlaced(GameEngine game) {
        if (masterRollPhase == MasterRollPhase.FADING)
            fadeMap.add(game.getLastLockedBlocks(), game.getLastLockedX(), game.getLastLockedY());
        checkSectionTransition();
        ruleset.updateLockDelay(getLockDelayForSpeedLevel(), 15);
        int pos;
        if (gravityAtFloor)        pos = 300;
        else if (gravityAtCeiling) pos = 0;
        else                       pos = Math.min(level - speedLevelStartLevel, 299);
        ruleset.updateGravity(currentSpdLv, pos);
        ruleset.are.trigger(false, System.nanoTime());
    }

    private void updateSpeedLevel() {
        int newSpdLv = calculateSpeedLevel();
        if (newSpdLv == currentSpdLv) return;
        currentSpdLv = newSpdLv;
        speedLevelStartLevel = level;
        ruleset.updateLockDelay(getLockDelayForSpeedLevel(), 15);
        ruleset.updateARE(currentSpdLv);
        ruleset.updateHandling(currentSpdLv);
        if (currentSpdLv == 2 && sectionHandler.missedCools() >= 2)
            ruleset.are.setOverride(2,
                    MasterARETable.getSpawnDelay(2),
                    MasterARETable.getLineClearDelay(2));
        else
            ruleset.are.clearOverrides();
    }

    private int calculateSpeedLevel() {
        int cools = sectionHandler.getCoolCount();

        // Cool-triggered early transitions take priority
        if (cools >= 6 && level >= 600) return 4;
        if (cools >= 4 && level >= 400) return 3;
        if (cools >= 2 && level >= 300) return 2;

        // Default transitions
        if (level >= 900) return 4;
        if (level >= 700) return 3;
        if (level >= 500) return 2;

        return 1;
    }

    @Override
    public void onLinesCleared(int lines, SpinType spin, int dropRows, TetrominoQueue.DropType dropType, GameEngine game) {
        hud.showClear(spin, lines, System.nanoTime());

        switch(lines) {
            case 1 -> level = Math.min(level + 1, 999);
            case 2 -> level = Math.min(level + 2, 999);
            case 3 -> level = Math.min(level + 3, 999);
            case 4 -> level = Math.min(level + 6, 999);
            default -> {}
        }
        checkSectionTransition();

        if (lines == 0) {
            gradeHandler.resetCombo();
            return;
        }

        ruleset.are.trigger(true, System.nanoTime());
        checkRollTransition(game);

        int tier = getTier(currentSpdLv);
        gradeHandler.decay(tier, masterRollPhase);
        gradeHandler.calculate(lines, spin, tier, masterRollPhase);


    }

    private void checkSectionTransition() {
        int currentSection = level / 100;

        // Evaluate at x80 mark - only once per section
        if (level % 100 >= 80 && lastEvaluatedSection < currentSection) {
            lastEvaluatedSection = currentSection;
            MasterSectionHandler.SectionResult result = sectionHandler.evaluateSection(currentSection, timer.getElapsedSeconds());

            if (result == MasterSectionHandler.SectionResult.COOL) {
                triggerCoolDisplay(System.nanoTime());
                if (gravityAtFloor) {
                    speedLevelStartLevel = level;
                    gravityAtFloor = false;
                    gravityAtCeiling = true;
                }
            } else if (result == MasterSectionHandler.SectionResult.REGRET) {
                triggerRegretDisplay(System.nanoTime());
                gradeHandler.applyRegret();
                currentSpdLv = Math.max(1, currentSpdLv - 1);
                speedLevelStartLevel = level;
                gravityAtFloor = true;
                gravityAtCeiling = false;
            }
        }

        // Section boundary - only update speed and gravity, no re-evaluation
        if (currentSection > lastSection) {
            lastSection = currentSection;
            sectionStartLevel = currentSection * 100;
            updateSpeedLevel();
            ruleset.updateGravity(currentSpdLv, level - sectionStartLevel);
        }
    }

    private long getLockDelayForSpeedLevel() {
        return switch(currentSpdLv) {
            case 2 -> 900_000_000L;
            case 3 -> 600_000_000L;
            case 4 -> 350_000_000L;
            default -> 1_150_000_000L;
        };
    }
    private void checkRollTransition(GameEngine game) {

        if (rollTriggered) return;
        if (level == 999) {
            rollTriggered = true;
            rollStartTime = System.nanoTime();
            MasterRollPhase newPhase = sectionHandler.meetsInvisibleRequirements(timer.getElapsedSeconds(), gradeHandler.getCurrentGrade())
                    ? MasterRollPhase.INVISIBLE
                    : MasterRollPhase.FADING;

            this.timer.pause();
            masterRollPhase = newPhase;
            gradeHandler.setPhase(newPhase, 0);
            fadeMap.triggerBoardFade(game.ROWS, newPhase);
        }
    }

    private void onRollComplete(GameEngine game) {
        double overflow = gradeHandler.getGradeOverflow();
        if(masterRollPhase == MasterRollPhase.INVISIBLE)
            gradeHandler.addGradeValue(1.5 + overflow);
        else
            gradeHandler.addGradeValue(0.75 + overflow);
        fadeMap.resetFadeStatus();
        game.clearGame();
    }
    @Override
    public void renderHUD(GraphicsContext g, GameTimer activeTimer, long now) {
        // -----------------------------------------------------------------
        // RENDER SPIN / LINE CLEAR NOTIFICATION
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
        final int HUD_X = 30;
        final int BAR_WIDTH = 180;

        if (activeTimer != null) {
            this.timer = activeTimer;
        }

        long timeMs = (timer != null) ? timer.getElapsedMs() : 0;
        String timeStr = GameTimer.formatTime(timeMs);
        g.save();
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        g.fillText("TIME", HUD_X, 150);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        g.fillText(timeStr, HUD_X, 175);
        g.restore();
        // -----------------------------------------------------------------
        // 2.5. RENDER COMBO COUNTER
        // -----------------------------------------------------------------
        if (gradeHandler.combo > 2) {
            g.save();

            // Hiệu ứng đổi màu: Combo càng cao màu càng cháy
            Color comboColor = switch (gradeHandler.combo) {
                case 1, 2 -> Color.YELLOW;
                case 3, 4 -> Color.ORANGE;
                case 5, 6 -> Color.RED;
                case 7, 8, 9 -> Color.PURPLE;
                default -> Color.CYAN;
            };

            // Canh ngay dưới B2B (B2B Y = 230, cộng thêm 25 pixel khoảng cách)
            double comboX = 195;
            double comboY = 375;

            g.setTextAlign(TextAlignment.CENTER);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18)); // Size 18 bằng chuẩn với B2B

            // Vẽ đổ bóng (Shadow)
            g.setFill(Color.color(0, 0, 0, 0.6));
            g.fillText(gradeHandler.combo + " COMBO", comboX + 2, comboY + 2);

            // Vẽ chữ chính
            g.setFill(comboColor);
            g.fillText(gradeHandler.combo + " COMBO", comboX, comboY);

            g.restore();
        }
        // -----------------------------------------------------------------
        // RENDER LEVEL PROGRESS
        // -----------------------------------------------------------------
        g.save();
        int displayLevel = Math.min(level, 999);
        int sectionStart = (displayLevel / 100) * 100;
        int sectionEnd = Math.min(sectionStart + 100, 999);

        double sectionPercent = (displayLevel == 999) ? 1.0 : (double) (displayLevel - sectionStart) / 100.0;

        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 12));
        g.fillText("LEVEL", HUD_X, 220);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        g.fillText(String.format("%03d", displayLevel), HUD_X, 245);
        g.setFill(Color.GRAY);
        g.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        g.fillText(" / " + sectionEnd, HUD_X + 40, 245);

        g.setFill(Color.rgb(40, 40, 40));
        g.fillRoundRect(HUD_X, 255, BAR_WIDTH, 8, 4, 4); // Background track
        if (sectionPercent > 0) {
            g.setFill(masterRollPhase != MasterRollPhase.NORMAL ? Color.PURPLE : Color.CYAN);
            g.fillRoundRect(HUD_X, 255, BAR_WIDTH * sectionPercent, 8, 4, 4);
        }
        g.restore();

        // -----------------------------------------------------------------
        // RENDER GRADE & INTERNAL PROGRESS
        // -----------------------------------------------------------------

        String letterGrade = gradeHandler.getCurrentGrade().label;
        double progress = Math.clamp(gradeHandler.getProgressToNextGrade(), 0.0, 1.0);

        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font("System", FontWeight.BOLD, 12));
        g.fillText("GRADE", HUD_X, 305);

        g.setFill(Color.ORANGE);
        g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));
        g.fillText(letterGrade, HUD_X, 335);

        g.setFill(Color.DARKGRAY);
        g.setFont(Font.font("Monospace", FontWeight.NORMAL, 12));
        g.fillText(String.format("(%.1f%%)", progress * 100), HUD_X + 60, 332);

        g.setFill(Color.rgb(40, 40, 40));
        g.fillRoundRect(HUD_X, 345, BAR_WIDTH, 8, 4, 4); // Background track
        if (progress > 0) {
            g.setFill(Color.ORANGE);
            g.fillRoundRect(HUD_X, 345, BAR_WIDTH * progress, 8, 4, 4);
        }
        g.restore();
        // ---------------------------------------------
        // Cool display
        // ---------------------------------------------
        if (shouldDisplayCool(now)) {
            g.save();
            g.setGlobalAlpha(getCoolAlpha(now));

            double boardX = 250;
            double boardWidth = 300;
            // Đã sửa: 50 (TOP_PANEL) + 600 (Chiều cao bảng) = 650
            double boardBottomY = 650;
            double bottomPanelHeight = 40; // Chiều cao thực tế có thể là 50 theo như GameRenderer bạn gửi

            double boxWidth = boardWidth - 10;
            double boxHeight = 28;
            double boxX = boardX + 5;

            double boxY = boardBottomY + (bottomPanelHeight - boxHeight) / 2.0;

            g.setFill(Color.color(0, 0.5, 1.0, 0.85));
            g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 6, 6);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);

            g.fillText("COOL!", boardX + boardWidth / 2.0, boxY + boxHeight / 2.0);
            g.restore();
        }

        // ---------------------------------------------
        // Regret display
        // ---------------------------------------------
        if (shouldDisplayRegret(now)) {
            g.save();
            g.setGlobalAlpha(getRegretAlpha(now));

            double boardX = 250;
            double boardWidth = 300;
            double boardBottomY = 650;
            double bottomPanelHeight = 40;

            double boxWidth = boardWidth - 10;
            double boxHeight = 28;
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
    public BoardRenderContext.CellAlphaProvider getAlphaProvider() {
        return masterRollPhase != MasterRollPhase.NORMAL ? fadeMap : null;
    }

    @Override
    public BoardRenderContext getBoardContext(TetrominoType[][] board) {
        return new BoardRenderContext(board, getAlphaProvider(), isBoardInvisible());
    }
    public boolean isBoardInvisible() {
        return masterRollPhase == MasterRollPhase.INVISIBLE;
    }

    @Override
    public FadeMap getFadeMap() { return fadeMap; }

    @Override
    public long getSortValue() {
        // Lấy Rank chuẩn từ Handler
        MasterRollPhase.GradeScale active = gradeHandler.getActiveGrade();

        // Tính toán điểm sắp xếp
        long baseSortValue = 0;

        if (active instanceof MasterGrade mg) {
            // MasterGrade: Từ G9 (0) đến M (27)
            baseSortValue = mg.ordinal();
        }
        else if (active instanceof MasterRollGrade rg) {
            // MasterRollGrade: Cộng dồn tiếp. MK (28) đến GM (33)
            baseSortValue = MasterGrade.values().length + rg.ordinal();
        }
        return (baseSortValue * 1000) + this.level;
    }

    @Override
    public String getDisplayValue() {
        return gradeHandler.getDisplayGrade();
    }
    ///// DEBUG: REMOVE IF DONE TESTING /////
    public void debugSetLevel(int targetLevel) {
        level = targetLevel;
        sectionStartLevel = (level / 100) * 100;
        speedLevelStartLevel = level;
        lastSection = level / 100;
        checkSectionTransition();
        ruleset.updateGravity(currentSpdLv, 0);
    }
}
