package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MasterModeHandler implements GameModeHandler{
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
    public boolean supportsPerfectClear() { return true; }

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
    public void onLinesCleared(int lines, SpinType spin, int dropRows, DropType dropType, GameEngine game) {
        level++;
        if (lines > 0 && level % 100 == 99)
            level++; // push past the cap on line clear
        checkRollTransition();
        hud.showClear(spin, lines, System.nanoTime());
        switch(lines) {
            case 1 -> level += 1;
            case 2 -> level += 2;
            case 3 -> level += 3;
            case 4 -> level += 6;
        }
        int tier = getTier(currentSpdLv);

        gradeHandler.decay(tier, masterRollPhase);

        if (lines == 0) {
            hud.showClear(spin, lines, System.nanoTime());
            gradeHandler.resetCombo();
            return;
        }
        gradeHandler.calculate(lines, spin, tier, masterRollPhase);
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
