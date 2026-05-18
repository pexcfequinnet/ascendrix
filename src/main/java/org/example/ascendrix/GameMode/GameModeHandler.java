package org.example.ascendrix.GameMode;

import javafx.scene.canvas.GraphicsContext;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoQueue;

public interface GameModeHandler {
    RulesetHandler getRuleset();
    default boolean supportsPerfectClear() { return false; }

    default void onPiecePlaced(GameEngine game) {}

    void onLinesCleared(int cleared, SpinType finalSpin, int pendingDropRows, TetrominoQueue.DropType pendingDropType, GameEngine gameEngine);
    void renderHUD(GraphicsContext g, GameTimer timer, long now);
    void setPerfectClearFlag(boolean flag);
}
