package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MarathonModeHandler implements GameModeHandler {
    private final PieceSpinHandler spinHandler = new SRSSpinDetector();
    private final HUDHandler hud = new HUDHandler();
    private final int targetLines;
    private int linesCleared = 0;
    private int score = 0;
    private int level = 1;
    public MarathonModeHandler(int targetLines) {
        this.targetLines = targetLines;
    }

    @Override
    public PieceSpinHandler getSpinHandler() {
        return spinHandler;
    }


    @Override
    public SpinType filterSpin(SpinType spin) {
        return spin;
    }

    @Override
    public void onLinesCleared(int lines, SpinType spin, GameEngine game) {
        linesCleared += lines;

        switch (lines) {
            case 1 -> score += 100;
            case 2 -> score += 300;
            case 3 -> score += 500;
            case 4 -> score += 800; // Tetris
        }

        level = linesCleared / 10 + 1;

        if (linesCleared >= targetLines) {
            game.end();
        }
    }

    @Override
    public HUDHandler getHUD() {
        hud.updateStats(linesCleared, targetLines, "", 0);
        return hud;
    }

    @Override
    public boolean isFinished() {
        return linesCleared >= targetLines;
    }

    @Override
    public void renderHUD(GraphicsContext g, GameTimer timer) {
        g.setFill(Color.WHITE);
        g.fillText("MARATHON (150 Lines)", 20, 400);

        long time = timer.getElapsedMs();
        g.fillText("Time: " + GameTimer.formatTime(time), 20, 420);
        g.fillText("Lines: " + linesCleared + "/" + targetLines, 20, 440);
        g.fillText("Score: " + score, 20, 460);
    }
}
