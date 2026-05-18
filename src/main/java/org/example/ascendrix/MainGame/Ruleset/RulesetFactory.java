package org.example.ascendrix.MainGame.Ruleset;

import org.example.ascendrix.GameMode.Marathon.MarathonRuleset;
import org.example.ascendrix.GameMode.Marathon.MasterRuleset;
import org.example.ascendrix.GameMode.Sprint.SprintRuleset;
import org.example.ascendrix.GameMode.GameMode;

public class RulesetFactory {
    public static RulesetHandler create(GameMode mode) {
        return switch (mode) {
            case SPRINT -> SprintRuleset.create();
            case MARATHON -> MarathonRuleset.create();
            case MASTER -> MasterRuleset.create();
            case OVERDRIVE -> null;
        };
    }
}
