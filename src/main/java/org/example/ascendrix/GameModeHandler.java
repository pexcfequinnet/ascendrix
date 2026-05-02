package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;

public interface GameModeHandler {
    SpinType filterSpin(SpinType spin);
    RulesetHandler getRuleset();
    void onLinesCleared(int cleared, SpinType finalSpin, int pendingDropRows, DropType pendingDropType, GameEngine gameEngine);
    PieceSpinHandler getSpinHandler();
    boolean isFinished();
    //void onTick(GameEngine engine, long now);
    HUDHandler getHUD();
    void renderHUD(GraphicsContext g,  GameTimer timer, long now);
}
