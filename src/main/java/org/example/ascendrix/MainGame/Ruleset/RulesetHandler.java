package org.example.ascendrix.MainGame.Ruleset;

import org.example.ascendrix.ARE.AREHandler;
import org.example.ascendrix.ARE.LockDelay;
import org.example.ascendrix.Movement.Gravity;
import org.example.ascendrix.Movement.Handling;
import org.example.ascendrix.Rotation.RotationSystem;

public class RulesetHandler {

    public final Handling handling;
    public final Gravity gravity;
    public final LockDelay lockDelay;
    public final AREHandler are;
    public final RotationSystem rotationSystem;

    public RulesetHandler(Handling handling,
                          Gravity gravity,
                          LockDelay lockDelay,
                          AREHandler are,
                          RotationSystem rotationSystem) {
        this.handling = handling;
        this.gravity = gravity;
        this.lockDelay = lockDelay;
        this.are = are;
        this.rotationSystem = rotationSystem;
    }
}