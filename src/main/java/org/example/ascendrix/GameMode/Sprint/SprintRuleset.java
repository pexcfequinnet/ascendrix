package org.example.ascendrix.GameMode.Sprint;

import org.example.ascendrix.ARE.AREHandler;
import org.example.ascendrix.ARE.LockDelayHandler;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Movement.GravityHandler;
import org.example.ascendrix.Movement.Handling;
import org.example.ascendrix.Movement.MovementConfig;
import org.example.ascendrix.Movement.MovementSystem;
import org.example.ascendrix.Rotation.RotationSystem;
import org.example.ascendrix.Rotation.SRS.StandardRotationSystem;

public class SprintRuleset extends RulesetHandler {
    private static final long GRAVITY_NS = 1_500_000_000; // 1.5s
    private static final long LOCK_NS    = 750_000_000; // 0.75s
    public SprintRuleset(Handling handling, RotationSystem rotationSystem) {
        super(
                handling,
                new GravityHandler(GRAVITY_NS),
                new LockDelayHandler(LOCK_NS, 15),
                new AREHandler(0,0),
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
