package org.example.ascendrix;

public class ModeHandlerFactory {
    public static GameModeHandler create(GameMode mode, GameTimer timer) {
        return switch (mode) {
            case SPRINT -> new SprintModeHandler(40);
            case MARATHON -> new MarathonModeHandler(150);
            case MASTER -> new MasterModeHandler(timer);
            case OVERDRIVE -> null;
        };
    }
}