package org.example.ascendrix;

public class SprintRuleset extends Ruleset{

    private static final int GRAVITY_FRAMES = 30;
    private static final int LOCK_FRAMES = 30;

    public SprintRuleset(Handling handling, RotationSystem rotationSystem) {
        super(
                handling,
                new SprintGravity(GRAVITY_FRAMES),
                new SprintLockDelay(LOCK_FRAMES),
                rotationSystem
        );
    }
}
