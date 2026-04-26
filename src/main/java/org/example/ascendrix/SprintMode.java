package org.example.ascendrix;

public class SprintMode implements GameMode {
    private final int targetLines;
    private int linesCleared = 0;

    public SprintMode(int targetLines) {
        this.targetLines = targetLines;
    }

    @Override
    public void onLinesCleared(int lines, GameEngine game) {
        linesCleared += lines;
        if (linesCleared >= targetLines) {
            game.end();
        }
    }

    @Override
    public boolean isFinished() {
        return linesCleared >= targetLines;
    }
}
