package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;

public interface GameModeHandler {
    SpinType filterSpin(SpinType spin);
    void onLinesCleared(int lines, SpinType spin, GameEngine game);
    PieceSpinHandler getSpinHandler();
    boolean isFinished();
    //void onTick(GameEngine engine, long now);
    HUDHandler getHUD();
    void renderHUD(GraphicsContext g,  GameTimer timer);
}
