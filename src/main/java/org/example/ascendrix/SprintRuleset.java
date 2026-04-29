package org.example.ascendrix;

public class SprintRuleset extends RulesetHandler {
    private static final long GRAVITY_NS = 500_000_000; // 0.5s
    private static final long LOCK_NS    = 500_000_000; // 0.5s
    public SprintRuleset(Handling handling, RotationSystem rotationSystem) {
        super(
                handling,
                new StandardGRaLockD(GRAVITY_NS),
                new SprintLockDelay(LOCK_NS),
                rotationSystem
        );
    }
    public static MovementConfig sprintConfig(){
        MovementConfig c = new MovementConfig();
        c.dasNs = 100_000_000;
        c.instantArr = true;
        c.instantSdf = true;

        return c;
    }
    public static SprintRuleset create() {
        return new SprintRuleset(
                new MovementSystem(sprintConfig()),
                new StandardRotationSystem()
        );
    }
}
