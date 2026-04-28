package org.example.ascendrix;

public class RulesetFactory {
    public static RulesetHandler create(GameMode mode) {
        return switch (mode) {
            case SPRINT -> SprintRuleset.create();
            /*
            case MARATHON -> StandardRuleset.create();
            case MASTER -> MasterRuleset.create();
            case OVERDRIVE -> StandardRuleset.create();
             */
            case MARATHON -> null;
            case MASTER -> null;
            case OVERDRIVE -> null;
        };
    }
}
