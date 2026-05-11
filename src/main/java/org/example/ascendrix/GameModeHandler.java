package org.example.ascendrix;

import javafx.scene.canvas.GraphicsContext;

public interface GameModeHandler {
    RulesetHandler getRuleset();
    default boolean supportsPerfectClear() { return false; }

    default void onPiecePlaced(GameEngine game) {}

    void onLinesCleared(int cleared, SpinType finalSpin, int pendingDropRows, DropType pendingDropType, GameEngine gameEngine);
    void renderHUD(GraphicsContext g,  GameTimer timer, long now);
    void setPerfectClearFlag(boolean flag);
}
