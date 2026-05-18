package org.example.ascendrix.GameMode.Master;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.ascendrix.GameMode.Marathon.MasterRuleset;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.GameMode.GameModeHandler;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.MainGame.Renderer.HUDHelper;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoQueue;

public class MasterModeHandler implements GameModeHandler {
    private final MasterRuleset ruleset;
    private final GameTimer timer;
    private final MasterGradeHandler gradeHandler = new MasterGradeHandler();
    private final HUDHelper hud = new HUDHelper();
    private MasterSectionHandler sectionHandler;
    private int currentSpdLv = 1;
    private long startTime = -1;
    private int level = 0;  
    private MasterRollPhase masterRollPhase = MasterRollPhase.NORMAL;
    private static final double[] SPEED_LEVEL = {1, 1.2, 1.4, 1.6};



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
        updateSpeedLevel();
        ruleset.updateGravity(level, currentSpdLv);
        ruleset.updateLockDelay(getLockDelayForSpeedLevel(), 15);
        ruleset.are.trigger(false, System.nanoTime()); // spawn ARE
    }

    private void updateSpeedLevel() {
        int newSpdLv = /* section cool trigger logic */ currentSpdLv;
        if (newSpdLv != currentSpdLv) // always false
            {
            currentSpdLv = newSpdLv;
            if (currentSpdLv == 2 && sectionHandler.missedCools() >= 2)
                ruleset.are.setOverride(2,
                        MasterARETable.getSpawnDelay(2),
                        MasterARETable.getLineClearDelay(2));
            else
                ruleset.are.clearOverrides();
        }
    }

    @Override
    public void onLinesCleared(int lines, SpinType spin, int dropRows, TetrominoQueue.DropType dropType, GameEngine game) {
        hud.showClear(spin, lines, System.nanoTime());

        switch(lines) {
            case 1 -> level += 2;
            case 2 -> level += 3;
            case 3 -> level += 4;
            case 4 -> level += 7;
            default -> level++;
        }

        if (lines == 0) {
            gradeHandler.resetCombo();
            return;
        }

        if (lines > 0)
            ruleset.are.trigger(true, System.nanoTime());


        checkRollTransition();

        int tier = getTier(currentSpdLv);
        gradeHandler.decay(tier, masterRollPhase);
        gradeHandler.calculate(lines, spin, tier, masterRollPhase);
    }


    private long getLockDelayForSpeedLevel() {
        return switch(currentSpdLv) {
            case 2 -> 600_000_000L;
            case 3 -> 400_000_000L;
            case 4 -> 175_000_000L;
            default -> 1_000_000_000L;
        };
    }
    private void checkRollTransition() {
        if (level == 999) {
            MasterRollPhase newPhase = sectionHandler.meetsInvisibleRequirements(timer.getElapsedSeconds())
                    ? MasterRollPhase.INVISIBLE
                    : MasterRollPhase.FADING;
            masterRollPhase = newPhase;
            gradeHandler.setPhase(newPhase);
        }
    }


    @Override
    public void renderHUD(GraphicsContext g, GameTimer timer, long now) {
        // Render spin
        if (hud.shouldDisplay(now)) {
            double alpha = hud.getAlpha(now);
            g.setGlobalAlpha(alpha);

            g.setFill(Color.color(0, 0, 0, 0.6));
            g.fillRect(160, 150, 70, 60);

            g.setFill(Color.ORANGE);
            g.fillText(hud.getClearText(), 160, 200);

            g.setGlobalAlpha(1.0);
        }

        // Letter grade
        g.setFill(Color.WHITE);
        g.setFont(javafx.scene.text.Font.font("Monospace", 24));
        String letterGrade = gradeHandler.getCurrentGrade().label;
        g.fillText(letterGrade, 240, 440);

        // Internal grade value and progress to next grade
        double currentGrade = gradeHandler.getGradeValue();
        double floor = gradeHandler.getCurrentFloor();
        double nextFloor = floor + gradeHandler.getCurrentGrade().threshold;

        if(floor >= nextFloor)
            floor -= nextFloor;

        // Internal grade value display
        double progress = (gradeHandler.getGradeValue() - floor) / (nextFloor - floor) * 100;
        if ((gradeHandler.getGradeValue() - floor) / (nextFloor - floor) * 100 >= 100)
            progress = progress - 100;
        g.setFont(javafx.scene.text.Font.font("Monospace", 12));
        g.fillText(String.format("%.2f / %.2f (%.1f%%)", currentGrade, nextFloor, progress), 240, 460);

    // Progress bar
        g.setFill(Color.DARKGRAY);
        g.fillRect(240, 465, 100, 6);
        g.setFill(Color.ORANGE);
        g.fillRect(240, 465, (int)(progress), 6);
        int sectionEnd = ((level / 100) + 1) * 100;
        int sectionProgress = level % 100;
        double sectionPercent = (double) sectionProgress / 100;

    // Numerator
        g.setFont(javafx.scene.text.Font.font("Monospace", 14));
        g.setFill(Color.WHITE);
        g.fillText("" + sectionProgress, 240, 500);

    // Progress bar as dividing line
        g.setFill(Color.DARKGRAY);
        g.fillRect(240, 504, 40, 3);
        g.setFill(Color.ORANGE);
        g.fillRect(240, 504, (int)(40 * sectionPercent), 3);

    // Denominator
        g.setFill(Color.WHITE);
        g.fillText("" + sectionEnd, 240, 520);
    }


}
