package org.example.ascendrix;

public class MarathonRuleset extends RulesetHandler {
    private static final long GRAVITY_NS = 500_000_000L;
    private static final long LOCK_NS = 500_000_000L; // 0.5s
    public MarathonRuleset(Handling handling, RotationSystem rotationSystem) {
        super(
                handling,
                new StandardGRaLockD(GRAVITY_NS),
                new SprintLockDelay(LOCK_NS),
                rotationSystem
        );
    }
    public static MovementConfig marathonConfig(){
        MovementConfig c = new MovementConfig();
        c.dasNs = 100_000_000L;
        c.arrNs = 20_000_000L;
        c.sdfNs = 30_000_000L;
        return c;
    }
    public static MarathonRuleset create() {
        return new MarathonRuleset(
                new MovementSystem(marathonConfig()),
                new StandardRotationSystem()
        );
    }
}