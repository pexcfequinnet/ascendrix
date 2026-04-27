package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;

public interface GameMode {
    void onLinesCleared(int lines, GameEngine game);
    boolean isFinished();
    HUDData getHUD();
    void renderHUD(GraphicsContext g,  GameTimer timer);
}
