package org.example.ascendrix.model.state;

public enum GamePhase {
    COUNTDOWN,
    PLAYING,
    PAUSED,
    ROLL,
    CLEARED,
    GAME_OVER;

    public boolean isPlayable() {
        return this == PLAYING || this == ROLL;
    }

    public boolean isTerminal() {
        return this == CLEARED || this == GAME_OVER;
    }
}