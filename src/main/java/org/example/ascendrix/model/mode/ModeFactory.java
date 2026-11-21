package org.example.ascendrix.model.mode;

import org.example.ascendrix.model.mechanics.HandlingConfig;

import java.util.Objects;

public final class ModeFactory {

    private ModeFactory() {
        // Utility factory class
    }

    public static ModeRuleset create(GameMode mode) {
        return create(mode, null);
    }

    public static ModeRuleset create(GameMode mode, HandlingConfig playerHandling) {
        Objects.requireNonNull(mode, "GameMode cannot be null");

        return switch (mode) {
            case SPRINT    -> SprintRuleset.create(playerHandling);
            case ULTRA     -> UltraRuleset.create();
            case MARATHON  -> MarathonRuleset.create(playerHandling);
            case MASTER    -> MasterRuleset.create();
            case OVERDRIVE -> OverdriveRuleset.create();
        };
    }
}