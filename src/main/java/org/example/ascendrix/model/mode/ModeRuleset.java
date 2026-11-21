package org.example.ascendrix.model.mode;

import org.example.ascendrix.model.board.FadeMap;
import org.example.ascendrix.model.mechanics.ARETimer;
import org.example.ascendrix.model.mechanics.GravityModel;
import org.example.ascendrix.model.mechanics.LockDelay;
import org.example.ascendrix.model.mechanics.MovementProcessor;
import org.example.ascendrix.model.mechanics.rotation.RotationSystem;
import org.example.ascendrix.model.mechanics.rotation.SpinType;
import org.example.ascendrix.model.piece.TetrominoQueue;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.state.GameSession.GameResult;
import org.example.ascendrix.model.state.HUDState;

public interface ModeRuleset {

    /* ========================== CORE MECHANICS ========================== */

    GameMode getMode();
    MovementProcessor getHandling();
    GravityModel getGravity();
    LockDelay getLockDelay();
    ARETimer getARE();
    RotationSystem getRotationSystem();
    HUDState getHUDState();

    /* ========================== LIFECYCLE HOOKS ========================== */

    void update(long now, GameSession session);

    default void onPieceSpawned(GameSession session) {}

    default void onPiecePlaced(GameSession session) {}

    default void setPerfectClearFlag(boolean perfectClear) {}

    void onLinesCleared(int lines,
                        SpinType spin,
                        int dropRows,
                        TetrominoQueue.DropType dropType,
                        GameSession session);

    /* ========================== CAPABILITIES & MODIFIERS ========================== */

    default boolean supportsIHS()          { return true; }
    default boolean supportsIRS()          { return true; }
    default boolean supportsPerfectClear() { return true; }

    default boolean isRollActive()         { return false; }
    default boolean isDecolorActive()      { return false; }
    default boolean isInvisible()          { return false; }

    default FadeMap getFadeMap()           { return null; }

    default void debugSetLevel(int level)  {}

    /* ========================== RUN RESULT ========================== */

    default GameResult getResult() {
        return new GameResult("", 0L);
    }
}