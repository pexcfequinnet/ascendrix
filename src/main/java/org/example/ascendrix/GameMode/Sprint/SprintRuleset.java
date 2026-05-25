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
    private static long frame_to_ns(double F) {
        return Math.round(1_000_000_000.0 * F / 60.0);
    }
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
        MovementConfig config = new MovementConfig();
        config.dasNs = Math.round(frame_to_ns(6));
        config.instantArr = true;
        config.instantSdf = true;

        return config;
    }
    public static SprintRuleset create() {
        return new SprintRuleset(
                new MovementSystem(sprintConfig()),
                new StandardRotationSystem()
        );
    }
}
