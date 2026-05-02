package org.example.ascendrix;

public class MarathonRuleset extends RulesetHandler {

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
        long ns = gravityNsForLevel(level);
        ((StandardGRaLockD) gravity).setFallNs(ns);
    }
    public MarathonRuleset(Handling handling, RotationSystem rotationSystem) {
        super(
                handling,
                new StandardGRaLockD(gravityNsForLevel(1)),
                new LockDelayConfig(1_000_000_000L),
                rotationSystem
        );
    }
    public static MovementConfig marathonConfig(){
        MovementConfig c = new MovementConfig();
        c.dasNs = 135_000_000L;
        c.arrNs = 20_000_000L;
        c.sdfNs = 25_000_000L;
        return c;
    }
    public static MarathonRuleset create() {
        return new MarathonRuleset(
                new MovementSystem(marathonConfig()),
                new StandardRotationSystem()
        );
    }
}
