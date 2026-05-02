package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SprintModeHandler implements GameModeHandler {
    private final SprintRuleset ruleset;
    private final int targetLines;
    private int linesCleared = 0;
    private final PieceSpinHandler spinHandler = new SRSSpinDetector();

    public SprintModeHandler(int targetLines) {
        this.ruleset = SprintRuleset.create();
        this.targetLines = targetLines;
    }
    private final HUDHandler hud = new HUDHandler();

    @Override
    public RulesetHandler getRuleset() { return ruleset; }

    @Override
    public PieceSpinHandler getSpinHandler() {
        return spinHandler;
    }


    @Override
    public SpinType filterSpin(SpinType spin) {
        return spin;
    }

    @Override
    public void onLinesCleared(int lines, SpinType finalSpin, int pendingDropRows, DropType pendingDropType, GameEngine game){
        linesCleared += lines;
        if (linesCleared >= targetLines) {
            game.end();
        }
    }

    @Override
    public boolean isFinished() {
        return linesCleared >= targetLines;
    }

    @Override
    public HUDHandler getHUD() {
        hud.updateStats(linesCleared, targetLines, "", 0);
        return hud;
    }
    @Override
    public void renderHUD(GraphicsContext g, GameTimer timer, long now) {
        g.setFill(Color.WHITE);
        g.fillText("SPRINT", 20, 400);

        long time = timer.getElapsedMs();
        g.fillText("Time: " + GameTimer.formatTime(time), 20, 420);
        g.fillText("Lines: " + linesCleared + "/" + targetLines, 20, 440);
    }
}