package org.example.ascendrix.GameMode.Marathon;

import org.example.ascendrix.ARE.AREHandler;
import org.example.ascendrix.ARE.LockDelayHandler;
import org.example.ascendrix.GameMode.Master.MasterARETable;
import org.example.ascendrix.GameMode.Master.MasterGravityTable;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Movement.GravityHandler;
import org.example.ascendrix.Movement.Handling;
import org.example.ascendrix.Movement.MovementConfig;
import org.example.ascendrix.Movement.MovementSystem;
import org.example.ascendrix.Rotation.RotationSystem;
import org.example.ascendrix.Rotation.SRS.StandardRotationSystem;

public class MasterRuleset extends RulesetHandler {

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
        config.dasNs = 135_000_000L;
        config.arrNs = 33_000_000L;

        return new MasterRuleset(
                new MovementSystem(config),
                new StandardRotationSystem()
        );
    }
}
