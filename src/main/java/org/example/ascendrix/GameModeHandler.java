package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;

public interface GameModeHandler {
    void onLinesCleared(int lines, GameEngine game);
    boolean isFinished();
    //void onTick(GameEngine engine, long now);
    HUDData getHUD();
    void renderHUD(GraphicsContext g,  GameTimer timer);
}
