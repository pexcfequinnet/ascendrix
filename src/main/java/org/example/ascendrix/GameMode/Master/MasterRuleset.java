package org.example.ascendrix.GameMode.Master;

import org.example.ascendrix.ARE.AREHandler;
import org.example.ascendrix.ARE.LockDelayHandler;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Movement.GravityHandler;
import org.example.ascendrix.Movement.Handling;
import org.example.ascendrix.Movement.MovementConfig;
import org.example.ascendrix.Movement.MovementSystem;
import org.example.ascendrix.Rotation.RotationSystem;
import org.example.ascendrix.Rotation.SRS.StandardRotationSystem;

public class MasterRuleset extends RulesetHandler {
    private static long frame_to_ns(double frames) {
        System.out.println(Math.round(1_000_000_000.0 * frames / 60.0));
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }
    public final AREHandler are = new AREHandler(
            MasterARETable.getSpawnDelay(0),
            MasterARETable.getLineClearDelay(0)
    );
    public MasterRuleset(Handling handling, RotationSystem rotationSystem) {
        super(
                handling,
                new GravityHandler(MasterGravityTable.toNs(MasterGravityTable.getGravity(0))),
                new LockDelayHandler(600_000_000L, 15),
                new AREHandler(MasterARETable.getSpawnDelay(1), MasterARETable.getLineClearDelay(1)),
                rotationSystem
        );
        updateHandling(1); // initialize with speed level 1 values
    }

    public void updateGravity(int level, int speedLevel) {
        int gravity = MasterGravityTable.getGravityWithMultiplier(level, speedLevel);
        long ns = MasterGravityTable.toNs(gravity);
        this.gravity.setFallNs(ns);

    }
    public void updateLockDelay(long delayNs, int maxMoves) {
        lockDelay.setLockNs(delayNs);
        lockDelay.setLockResetLimit(maxMoves);
    }

    public void updateARE(int level) {
        are.setSpawnDelayNs(MasterARETable.getSpawnDelay(level));
        are.setLineClearDelayNs(MasterARETable.getLineClearDelay(level));
    }

    public static MasterRuleset create() {
        MovementConfig config = new MovementConfig();
        return new MasterRuleset(
                new MovementSystem(config),
                new StandardRotationSystem()
        );
    }

    public void updateHandling(int speedLevel) {
        MovementConfig config = ((MovementSystem) handling).config;
        switch (speedLevel) {
            case 1 -> {
                config.dasNs = frame_to_ns(16);
                config.arrNs = frame_to_ns(1);
            }
            case 2 -> {
                config.dasNs = frame_to_ns(10);
                config.arrNs = frame_to_ns(0.75);
            }
            case 3 -> {
                config.dasNs = frame_to_ns(8);
                config.arrNs = frame_to_ns(0.5);
            }
            case 4 -> {
                config.dasNs = frame_to_ns(6);
                config.arrNs = frame_to_ns(0.25);
            }
        }
    }
}
