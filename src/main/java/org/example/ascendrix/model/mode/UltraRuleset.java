package org.example.ascendrix.model.mode;

import org.example.ascendrix.model.mechanics.ARETimer;
import org.example.ascendrix.model.mechanics.GravityModel;
import org.example.ascendrix.model.mechanics.HandlingConfig;
import org.example.ascendrix.model.mechanics.LockDelay;
import org.example.ascendrix.model.mechanics.LockDelayTimer;
import org.example.ascendrix.model.mechanics.MovementProcessor;
import org.example.ascendrix.model.mechanics.rotation.RotationSystem;
import org.example.ascendrix.model.mechanics.rotation.SpinType;
import org.example.ascendrix.model.mechanics.rotation.SRS;
import org.example.ascendrix.model.piece.TetrominoQueue;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.state.GameSession.GameResult;

public class UltraRuleset extends BaseModeRuleset {

    public static final long DEFAULT_DURATION_MS = 120_000L; // 2 minutes
    private static final int FIXED_LEVEL         = 1;
    private static final int NO_TARGET_LINES     = 0;

    /* ========================== TIMING & HANDLING CONSTANTS ========================== */

    private static final long LEVEL_1_GRAVITY_NS   = 1_000_000_000L; // 1.0s per row

    private static final long LOCK_DELAY_NS        = 600_000_000L;   // 600 ms
    private static final int  LOCK_RESET_LIMIT     = 15;

    private static final long ARE_SPAWN_NS         = framesToNs(0.5);  // 0.5F (8,333,333 ns)
    private static final long ARE_LINE_CLEAR_NS    = framesToNs(0.5);  // 0.5F (8,333,333 ns)
    private static final long CLEAR_ANIM_NS        = 150_000_000L;   // 150 ms

    private static final long ULTRA_DAS_NS         = framesToNs(7.5);  // 7.5F (125 ms)
    private static final long ULTRA_ARR_NS         = framesToNs(1.2);  // 1.2F (20 ms)
    private static final long ULTRA_SDF_NS         = framesToNs(0.6);  // 0.6F (10 ms)

    private static long framesToNs(double frames) {
        return Math.round(1_000_000_000.0 * frames / 60.0);
    }

    /* ========================== SCORING TUNING CONSTANTS ========================== */

    private static final double BASE_CLEAR_POINTS        = 4000.0;
    private static final double LINE_SCALING_EXPONENT    = 1.25;
    private static final int    PERFECT_CLEAR_LINE_BONUS = 1200;

    private static final int    TSPIN_BASE_POINTS        = 240;
    private static final int    MINI_SPIN_BASE_POINTS    = 115;

    private static final int    DROP_POINTS_PER_ROW      = 5;
    private static final int    MAX_COMBO                = 25;
    private static final double COMBO_LOG_BASE           = 10_000.0;

    private static final int[]    B2B_THRESHOLDS   = { 3, 7, 15, 31, 63 };
    private static final double[] B2B_TIERS        = { 1.2, 1.6, 2.4, 4.0, 7.2 };
    private static final double[] MINI_B2B_TIERS   = { 1.1, 1.3, 1.7, 2.5, 4.1 };

    /* ========================== STATE ========================== */

    private final long durationMs;
    private int linesCleared = 0;
    private long score = 0L;
    private int combo = 0;
    private boolean b2bActive = false;
    private int b2bStreak = 0;
    private boolean perfectClear = false;
    private boolean completed = false;
    private long lastNowNs = 0L;

    private UltraRuleset(long durationMs,
                         MovementProcessor handling,
                         GravityModel gravity,
                         LockDelay lockDelay,
                         ARETimer are,
                         RotationSystem rotationSystem) {
        super(GameMode.ULTRA, handling, gravity, lockDelay, are, rotationSystem);
        this.durationMs = Math.max(1_000L, durationMs);
        syncHUD(this.durationMs);
    }

    public static UltraRuleset create() {
        return create(DEFAULT_DURATION_MS);
    }

    public static UltraRuleset create(long durationMs) {
        HandlingConfig forcedConfig = new HandlingConfig(
                ULTRA_DAS_NS,
                ULTRA_ARR_NS,
                ULTRA_SDF_NS,
                false,
                false
        );

        return new UltraRuleset(
                durationMs,
                new MovementProcessor(forcedConfig),
                GravityModel.fromFallNs(LEVEL_1_GRAVITY_NS),
                new LockDelayTimer(LOCK_DELAY_NS, LOCK_RESET_LIMIT),
                new ARETimer(ARE_SPAWN_NS, ARE_LINE_CLEAR_NS, CLEAR_ANIM_NS),
                new SRS()
        );
    }
    /* ========================== LIFECYCLE HOOKS ========================== */

    @Override
    public void update(long now, GameSession session) {
        this.lastNowNs = now;
        long elapsedMs = session.getTimer().getElapsedMs(now);
        long remainingMs = Math.max(0L, durationMs - elapsedMs);

        syncHUD(remainingMs);

        if (!completed && elapsedMs >= durationMs) {
            completed = true;
            session.clearGame();
        }
    }

    @Override
    public void setPerfectClearFlag(boolean perfectClear) {
        this.perfectClear = perfectClear;
    }

    @Override
    public void onLinesCleared(int lines,
                               SpinType spin,
                               int dropRows,
                               TetrominoQueue.DropType dropType,
                               GameSession session) {
        long eventNow = (lastNowNs > 0L) ? lastNowNs : System.nanoTime();
        long elapsedMs = session.getTimer().getElapsedMs(eventNow);
        long remainingMs = Math.max(0L, durationMs - elapsedMs);

        // Drop points apply regardless of whether lines were cleared
        score += calculateDropScore(dropRows, dropType);

        if (lines <= 0) {
            combo = 0;
            this.perfectClear = false;
            syncHUD(remainingMs);
            hudState.recordClear(spin, 0, eventNow);
            return;
        }

        // 1. Update line count (no level scaling in Ultra)
        linesCleared += lines;

        // 2. Update Back-to-Back streak
        boolean isB2B = (lines == 4) || (spin != null && spin != SpinType.NONE);
        b2bStreak = (isB2B && b2bActive) ? (b2bStreak + 1) : 0;
        b2bActive = isB2B;

        // 3. Award clear score (without level multiplier) & advance combo
        score += calculateClearScore(lines, spin, isB2B, perfectClear);
        combo = Math.min(combo + 1, MAX_COMBO);
        this.perfectClear = false;

        // 4. Sync HUD
        syncHUD(remainingMs);
        hudState.recordClear(spin, lines, eventNow);
    }

    /* ========================== SCORING HELPERS ========================== */

    private long calculateDropScore(int dropRows, TetrominoQueue.DropType dropType) {
        if (dropRows <= 0 || dropType == null || dropType == TetrominoQueue.DropType.NONE) {
            return 0L;
        }
        return (long) dropRows * DROP_POINTS_PER_ROW;
    }

    private long calculateClearScore(int lines, SpinType spin, boolean isB2B, boolean isPerfectClear) {
        int tier = getB2BTier(b2bStreak);

        double linePoints = getLinePoints(lines, isPerfectClear);
        double spinPoints = getSpinPoints(spin);

        double spinMult  = getSpinMultiplier(spin, tier);
        double b2bMult   = getB2BMultiplier(isB2B, tier);
        double comboMult = getComboMultiplier(combo);

        double total = BASE_CLEAR_POINTS
                + (linePoints + spinPoints) * spinMult * b2bMult * comboMult;

        return Math.round(total);
    }

    private double getLinePoints(int lines, boolean isPerfectClear) {
        double effectiveLines = lines + (isPerfectClear ? PERFECT_CLEAR_LINE_BONUS : 0);
        return Math.pow(effectiveLines / 2.0, LINE_SCALING_EXPONENT);
    }

    private double getSpinPoints(SpinType spin) {
        if (spin == null || spin == SpinType.NONE) return 0.0;
        return (spin == SpinType.T_SPIN) ? TSPIN_BASE_POINTS : MINI_SPIN_BASE_POINTS;
    }

    private double getSpinMultiplier(SpinType spin, int tier) {
        if (spin == null || spin == SpinType.NONE) return 1.0;
        return (spin == SpinType.T_SPIN) ? B2B_TIERS[tier] : MINI_B2B_TIERS[tier];
    }

    private double getB2BMultiplier(boolean isB2B, int tier) {
        return isB2B ? B2B_TIERS[tier] : 1.0;
    }

    private double getComboMultiplier(int currentCombo) {
        return logBase(currentCombo + 1, COMBO_LOG_BASE) + 1.0;
    }

    private static double logBase(double value, double base) {
        return Math.log(value) / Math.log(base);
    }

    public static int getB2BTier(int streak) {
        int tier = 0;
        for (int threshold : B2B_THRESHOLDS) {
            if (streak >= threshold) tier++;
            else break;
        }
        return tier;
    }

    /* ========================== HUD & RESULT ========================== */

    private void syncHUD(long remainingMs) {
        hudState.updateStats(FIXED_LEVEL, NO_TARGET_LINES, "", remainingMs);
        hudState.setLinesCleared(linesCleared);
        hudState.setScore(score);
        hudState.setCombo(combo);
        hudState.setB2bStreak(b2bActive ? b2bStreak : 0);
    }

    @Override
    public GameResult getResult() {
        return new GameResult(String.format("%,d", score), score);
    }

    public long getDurationMs()  { return durationMs; }
    public int getLinesCleared() { return linesCleared; }
    public long getScore()       { return score; }
    public int getCombo()        { return combo; }
    public int getB2bStreak()    { return b2bActive ? b2bStreak : 0; }
}