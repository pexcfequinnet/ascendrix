package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SprintMode implements GameMode {
    private final int targetLines;
    private int linesCleared = 0;
    private GameEngine game;

    public SprintMode(int targetLines) {
        this.targetLines = targetLines;
        this.game = game;
    }
    @Override
    public void onLinesCleared(int lines, GameEngine game) {
        linesCleared += lines;
        if (linesCleared >= targetLines) {
            game.end();
        }
    }

    @Override
    public HUDData getHUD() {
        return new HUDData(linesCleared, targetLines, "", 0 );
    }
    @Override
    public boolean isFinished() {
        return linesCleared >= targetLines;
    }

    @Override
    public void renderHUD(GraphicsContext g, GameTimer timer) {
        g.setFill(Color.WHITE);
        g.fillText("SPRINT", 20, 400);

        long time = timer.getElapsedMs();
        g.fillText("Time: " + GameTimer.formatTime(time), 20, 420);
        g.fillText("Lines: " + linesCleared + "/" + targetLines, 20, 440);
    }
}