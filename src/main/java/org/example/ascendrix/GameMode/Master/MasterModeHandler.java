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
        level++;
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

        if (lines == 0) {
            gradeHandler.resetCombo();
            return;
        }

        if (lines > 0)
            ruleset.are.trigger(true, System.nanoTime());

        switch(lines) {
            case 1 -> level += 1;
            case 2 -> level += 2;
            case 3 -> level += 3;
            case 4 -> level += 6;
        }

        checkRollTransition();

        int tier = getTier(currentSpdLv);
        gradeHandler.decay(tier, masterRollPhase);
        gradeHandler.calculate(lines, spin, tier, masterRollPhase);
    }


    private long getLockDelayForSpeedLevel() {
        return switch(currentSpdLv) {
            case 1 -> 600_000_000L;  // 600ms - default
            case 2 -> 400_000_000L;  // 400ms - tightened
            case 3 -> 200_000_000L;  // 200ms - tightened further
            case 4 -> 100_000_000L;  // 100ms - very tight
            default -> 600_000_000L;
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
        g.setFill(Color.WHITE);
        g.fillText("Level: " + level, 240, 420);
    }

}
