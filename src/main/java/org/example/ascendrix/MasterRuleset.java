package org.example.ascendrix;

public class MasterRuleset extends RulesetHandler{

    public MasterRuleset(Handling handling, RotationSystem rotationSystem) {
        super(
                handling,
                new GravityHandler(1_400_000_000L),
                new LockDelayHandler(600_000_000L, 15),
                rotationSystem
        );
    }

    public static MasterRuleset create() {
        MovementConfig config = new MovementConfig();
        config.dasNs = 135_000_000L;
        config.arrNs = 33_000_000L;

        return new MasterRuleset(
                new MovementSystem(config),
                new StandardRotationSystem()
        );
    }
}
