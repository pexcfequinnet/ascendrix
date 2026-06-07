package org.example.ascendrix.GameMode;

import javafx.scene.canvas.GraphicsContext;
import org.example.ascendrix.GameMode.Master.FadeMap;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.MainGame.Renderer.BoardRenderContext;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoQueue;
import org.example.ascendrix.Tetromino.TetrominoType;

public interface GameModeHandler {
    RulesetHandler getRuleset();
    default boolean supportsPerfectClear() { return false; }

    default boolean supportsIRS() { return true; }
    default boolean supportsIHS() { return true; }
    default void onPiecePlaced(GameEngine game) {}
    default void update(long now, GameEngine game) {}
    void onLinesCleared(int cleared, SpinType finalSpin, int pendingDropRows, TetrominoQueue.DropType pendingDropType, GameEngine gameEngine);
    void renderHUD(GraphicsContext g, GameTimer timer, long now);
    void setPerfectClearFlag(boolean flag);
    default void debugSetLevel(int level){}
    default void onPieceSpawned(GameEngine game) {};
    default BoardRenderContext getBoardContext(TetrominoType[][] board) {
        return new BoardRenderContext(board, null, false);
    }
    default FadeMap getFadeMap() { return null; }
    default BoardRenderContext.CellAlphaProvider getAlphaProvider() { return null; }

}
