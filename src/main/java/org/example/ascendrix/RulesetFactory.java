package org.example.ascendrix;

public class RulesetFactory {
    public static RulesetHandler create(GameMode mode) {
        return switch (mode) {
            case SPRINT -> SprintRuleset.create();
            case MARATHON -> MarathonRuleset.create();
            case MASTER -> null;
            case OVERDRIVE -> null;
        };
    }
}
