package org.example.ascendrix;

public interface GameMode {
    void onLinesCleared(int lines, GameEngine game);
    boolean isFinished();
}
