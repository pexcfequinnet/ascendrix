package org.example.ascendrix.GameMode.Overdrive;


import org.example.ascendrix.ARE.AREHandler;
import org.example.ascendrix.ARE.LockDelayHandler;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Movement.GravityHandler;
import org.example.ascendrix.Movement.Handling;
import org.example.ascendrix.Movement.MovementConfig;
import org.example.ascendrix.Movement.MovementSystem;
import org.example.ascendrix.Rotation.RotationSystem;
import org.example.ascendrix.Rotation.SRS.StandardRotationSystem;

public class OverdriveRuleset extends RulesetHandler {
    private static long frame_to_ns(double frames) {
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }

    private static final long GRAVITY_20G = frame_to_ns(1.0 / 20.0);

    public OverdriveRuleset(Handling handling, RotationSystem rotationSystem) {
        super(
                handling,
                new GravityHandler(GRAVITY_20G),
                new LockDelayHandler(OverdriveLockTable.getLockDelay(0), 15),
                new AREHandler(
                        OverdriveARETable.getSpawnDelay(0),
                        OverdriveARETable.getLineClearDelay(0),
                        OverdriveARETable.getClearAnimDelay(0)
                ),
                rotationSystem
        );
        updateHandling(0);
    }
    public void updateHandling(int level) {
        MovementConfig config = ((MovementSystem) handling).config;
        config.dasNs = OverdriveHandlingTable.getDAS(level);
        config.arrNs = OverdriveHandlingTable.getARR(level);
    }

    public void updateARE(int level) {
        are.setSpawnDelayNs(OverdriveARETable.getSpawnDelay(level));
        are.setLineClearDelayNs(OverdriveARETable.getLineClearDelay(level));
        are.setClearAnimNs(OverdriveARETable.getClearAnimDelay(level));
    }

    public void updateLockDelay(int level) {
        lockDelay.setLockNs(OverdriveLockTable.getLockDelay(level));
    }

    public void updateAll(int level) {
        updateARE(level);
        updateLockDelay(level);
        updateHandling(level);
    }

    public static OverdriveRuleset create() {
        return new OverdriveRuleset(
                new MovementSystem(new MovementConfig()),
                new StandardRotationSystem()
        );
    }
}
