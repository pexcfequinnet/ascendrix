package org.example.ascendrix;

public class RulesetHandler {

    public final Handling handling;
    public final Gravity gravity;
    public final LockDelay lockDelay;
    public final RotationSystem rotationSystem;

    public RulesetHandler(Handling handling,
                          Gravity gravity,
                          LockDelay lockDelay,
                          RotationSystem rotationSystem) {
        this.handling = handling;
        this.gravity = gravity;
        this.lockDelay = lockDelay;
        this.rotationSystem = rotationSystem;
    }
}