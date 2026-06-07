package org.example.ascendrix.GameMode.Marathon;

import org.example.ascendrix.ARE.AREHandler;
import org.example.ascendrix.ARE.LockDelayHandler;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Movement.GravityHandler;
import org.example.ascendrix.Movement.Handling;
import org.example.ascendrix.Movement.MovementConfig;
import org.example.ascendrix.Movement.MovementSystem;
import org.example.ascendrix.Rotation.RotationSystem;
import org.example.ascendrix.Rotation.SRS.StandardRotationSystem;

public class MarathonRuleset extends RulesetHandler {
    private final MovementConfig config;
    private static final double[] GRAVITY_SECONDS = computeGravityTable();
    private static double[] computeGravityTable() {
        double[] table = new double[20 + 1];
        for (int level = 1; level <= 20; level++) {
            table[level] = Math.pow(0.8 - ((level - 1) * 0.007), level - 1);
        }
        return table;
    }

    public static long gravityNsForLevel(int level)  {
        int clamped = Math.clamp(level, 1, GRAVITY_SECONDS.length - 1);
        return (long) (GRAVITY_SECONDS[clamped] * 1_000_000_000L);
    }

    public void onLevelChanged(int level) {
        long gravityNs = gravityNsForLevel(level);
        gravity.setFallNs(gravityNs);
        config.sdfNs = gravityNs / 20;
        if (level >= 5) {
            config.dasNs = 122_000_000L; // faster DAS at lv5+
            config.arrNs = 25_000_000L;  // faster ARR at lv5+
        }
        if (level >= 12){
            config.dasNs = 110_000_000L; // faster DAS at lv5+
            config.arrNs = 16_000_000L;  // faster ARR at lv5+
        }
    }

    public MarathonRuleset(Handling handling, RotationSystem rotationSystem, MovementConfig config) {
        this.config = config;
        super(
                handling,
                new GravityHandler(gravityNsForLevel(1)),
                new LockDelayHandler(600_000_000L, 15),
                new AREHandler(100_000_000L, 50_000_000L, 150_000_000L),


                rotationSystem
        );
        ((LockDelayHandler) lockDelay).setLockResetLimit(15);
        config.sdfNs = gravityNsForLevel(1) / 20; // set initial SDF at level 1
    }

    public static MarathonRuleset create() {
        MovementConfig config = new MovementConfig();
        config.dasNs = 135_000_000L;
        config.arrNs = 33_000_000L;
        // sdfNs set dynamically in constructor

        return new MarathonRuleset(
                new MovementSystem(config),
                new StandardRotationSystem(),
                config
        );
    }
}
