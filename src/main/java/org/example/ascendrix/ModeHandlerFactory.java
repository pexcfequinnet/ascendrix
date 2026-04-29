package org.example.ascendrix;

public class ModeHandlerFactory {
    public static GameModeHandler create(GameMode mode) {
        return switch (mode) {
            case SPRINT -> new SprintModeHandler(40);
            /*
            case MASTER -> new MasterModeHandler();
            case MARATHON -> new MarathonModeHandler();
             */
            case MARATHON -> new MarathonModeHandler(150);
            case MASTER -> null;
            case OVERDRIVE -> null;
        };
    }
}