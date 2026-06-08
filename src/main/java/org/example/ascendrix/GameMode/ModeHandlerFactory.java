package org.example.ascendrix.GameMode;

import org.example.ascendrix.GameMode.Marathon.MarathonModeHandler;
import org.example.ascendrix.GameMode.Master.MasterModeHandler;
import org.example.ascendrix.GameMode.Overdrive.OverdriveModeHandler;
import org.example.ascendrix.GameMode.Sprint.SprintModeHandler;
import org.example.ascendrix.MainGame.Engine.GameTimer;

public class ModeHandlerFactory {
    public static GameModeHandler create(GameMode mode, GameTimer timer) {
        return switch (mode) {
            case SPRINT -> new SprintModeHandler(40);
            case MARATHON -> new MarathonModeHandler(200);
            case MASTER -> new MasterModeHandler(timer);
            case OVERDRIVE -> new OverdriveModeHandler(timer);
        };
    }
}