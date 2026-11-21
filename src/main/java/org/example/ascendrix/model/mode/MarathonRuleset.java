package org.example.ascendrix.model.mode;

import org.example.ascendrix.model.mechanics.ARETimer;
import org.example.ascendrix.model.mechanics.GravityModel;
import org.example.ascendrix.model.mechanics.HandlingConfig;
import org.example.ascendrix.model.mechanics.LockDelay;
import org.example.ascendrix.model.mechanics.LockDelayTimer;
import org.example.ascendrix.model.mechanics.MovementProcessor;
import org.example.ascendrix.model.mechanics.rotation.RotationSystem;
import org.example.ascendrix.model.mechanics.rotation.SRS;
import org.example.ascendrix.model.mechanics.rotation.SpinType;
import org.example.ascendrix.model.piece.TetrominoQueue;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.state.GameSession.GameResult;

public class MarathonRuleset extends BaseModeRuleset {

    public static final int DEFAULT_TARGET_LINES = 200;
    public static final int MAX_LEVEL = 20;
    private static final int LINES_PER_LEVEL = 10;
    private static final double GRAVITY_20G_SECONDS = 1.0 / (60.0 * 20.0);

    /* ========================== TIMING & HANDLING CONSTANTS ========================== */

    private static final long LOCK_DELAY_NS        = 600_000_000L; // 600 ms
    private static final int  LOCK_RESET_LIMIT     = 15;

    private static final long ARE_SPAWN_NS         = 100_000_000L; // 100 ms
    private static final long ARE_LINE_CLEAR_NS    = 50_000_000L;  // 50 ms
    private static final long CLEAR_ANIM_NS        = 150_000_000L; // 150 ms

    private static final long DEFAULT_DAS_NS       = 135_000_000L; // 135 ms (Lv 1-4)
    private static final long DEFAULT_ARR_NS       = 33_000_000L;  // 33 ms  (Lv 1-4)
    private static final long LV5_DAS_NS           = 122_000_000L; // 122 ms (Lv 5-11)
    private static final long LV5_ARR_NS           = 25_000_000L;  // 25 ms  (Lv 5-11)
    private static final long LV12_DAS_NS          = 110_000_000L; // 110 ms (Lv 12+)
    private static final long LV12_ARR_NS          = 16_000_000L;  // 16 ms  (Lv 12+)
    private static final int  SDF_DIVISOR          = 20;

    private static final double[] GRAVITY_SECONDS  = computeGravityTable();

    /* ========================== SCORING TUNING CONSTANTS ========================== */

    private static final double BASE_CLEAR_POINTS        = 1500.0;
    private static final double LINE_SCALING_EXPONENT    = 1.25;
    private static final int    PERFECT_CLEAR_LINE_BONUS = 900;

    private static final int    TSPIN_BASE_POINTS        = 230;
    private static final int    MINI_SPIN_BASE_POINTS    = 115;

    private static final int    DROP_POINTS_PER_ROW      = 5;
    private static final int    COMBO_START_COUNT        = 2;
    private static final int    COMBO_CAP_COUNT          = 20;
    private static final double MAX_COMBO_MULTIPLIER     = 1.25;

    private static final int    MULTIPLIER_CAP_LEVEL = 20;
    private static final double MAX_LEVEL_MULTIPLIER = 2.0;

    private static final int[]    B2B_THRESHOLDS   = { 3, 7, 15, 31, 63 };
    private static final double[] B2B_TIERS        = { 1.2, 1.4, 1.8, 2.6, 3.2 };
    private static final double[] MINI_B2B_TIERS   = { 1.1, 1.2, 1.4, 1.8, 2.6 };

    /* ========================== STATE ========================== */

    private final HandlingConfig config;
    private final long baseDasNs;
    private final long baseArrNs;

    private final int targetLines;
    private int linesCleared = 0;
    private int level = 1;
    private long score = 0L;
    private int combo = 0;
    private boolean b2bActive = false;
    private int b2bStreak = 0;
    private boolean perfectClear = false;
    private long lastNowNs = 0L;

    private MarathonRuleset(int targetLines,
                            HandlingConfig config,
                            MovementProcessor handling,
                            GravityModel gravity,
                            LockDelay lockDelay,
                            ARETimer are,
                            RotationSystem rotationSystem) {
        super(GameMode.MARATHON, handling, gravity, lockDelay, are, rotationSystem);
        this.targetLines = targetLines;
        this.config = config;
        this.baseDasNs = config.getDasNs();
        this.baseArrNs = config.getArrNs();

        // Initialize Level 1 gravity and SDF
        onLevelChanged(1);
        syncHUD(0L);
    }


    public static MarathonRuleset create() {
        return create(DEFAULT_TARGET_LINES, null);
    }

    public static MarathonRuleset create(HandlingConfig playerHandling) {
        return create(DEFAULT_TARGET_LINES, playerHandling);
    }

    public static MarathonRuleset create(int targetLines, HandlingConfig playerHandling) {
        // Clone player config (or use Marathon defaults) so dynamic level speed-ups don't mutate saved settings
        HandlingConfig config = (playerHandling != null)
                ? playerHandling.copy()
                : new HandlingConfig(
                DEFAULT_DAS_NS,
                DEFAULT_ARR_NS,
                gravityNsForLevel(1) / SDF_DIVISOR,
                false,
                false
        );
        return new MarathonRuleset(
                targetLines,
                config,
                new MovementProcessor(config),
                GravityModel.fromFallNs(gravityNsForLevel(1)),
                new LockDelayTimer(LOCK_DELAY_NS, LOCK_RESET_LIMIT),
                new ARETimer(ARE_SPAWN_NS, ARE_LINE_CLEAR_NS, CLEAR_ANIM_NS),
                new SRS()
        );
    }

    /* ========================== GRAVITY & LEVEL SCALING ========================== */

    private static double[] computeGravityTable() {
        double[] table = new double[MAX_LEVEL + 1];
        for (int lvl = 1; lvl <= MAX_LEVEL; lvl++) {
            if (lvl > 19) {
                table[lvl] = GRAVITY_20G_SECONDS;
            } else {
                table[lvl] = Math.pow(0.8 - ((lvl - 1) * 0.007), lvl - 1);
            }
        }
        return table;
    }
    public static long gravityNsForLevel(int level) {
        int clamped = Math.clamp(level, 1, GRAVITY_SECONDS.length - 1);
        return (long) (GRAVITY_SECONDS[clamped] * 1_000_000_000L);
    }

    public void onLevelChanged(int newLevel) {
        long gravityNs = gravityNsForLevel(newLevel);
        gravity.setFallNs(gravityNs);
        config.setSdfNs(gravityNs / SDF_DIVISOR);

        if (newLevel >= 12) {
            config.setDasNs(Math.min(baseDasNs, LV12_DAS_NS));
            config.setArrNs(Math.min(baseArrNs, LV12_ARR_NS));
        } else if (newLevel >= 5) {
            config.setDasNs(Math.min(baseDasNs, LV5_DAS_NS));
            config.setArrNs(Math.min(baseArrNs, LV5_ARR_NS));
        } else {
            config.setDasNs(baseDasNs);
            config.setArrNs(baseArrNs);
        }
    }

    @Override
    public void debugSetLevel(int targetLevel) {
        this.level = Math.clamp(targetLevel, 1, MAX_LEVEL);
        this.linesCleared = (this.level - 1) * LINES_PER_LEVEL;
        onLevelChanged(this.level);
        syncHUD(hudState.getTimeMs());
    }

    /* ========================== LIFECYCLE HOOKS ========================== */

    @Override
    public void update(long now, GameSession session) {
        this.lastNowNs = now;
        syncHUD(session.getTimer().getElapsedMs(now));
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

        // Drop points apply regardless of whether lines were cleared
        score += calculateDropScore(dropRows, dropType);

        if (lines <= 0) {
            combo = 0;
            this.perfectClear = false;
            syncHUD(elapsedMs);
            hudState.recordClear(spin, 0, eventNow);
            return;
        }

        // 1. Update line count & level speed curve
        linesCleared += lines;
        int newLevel = Math.min(MAX_LEVEL, (linesCleared / LINES_PER_LEVEL) + 1);
        if (newLevel != level) {
            level = newLevel;
            onLevelChanged(level);
        }

        // 2. Update Back-to-Back streak
        boolean isB2B = (lines == 4) || (spin != null && spin != SpinType.NONE);
        b2bStreak = (isB2B && b2bActive) ? (b2bStreak + 1) : 0;
        b2bActive = isB2B;

        // 3. Award clear score & advance combo
        combo++;
        score += calculateClearScore(lines, spin, isB2B, perfectClear);
        this.perfectClear = false;

        // 4. Sync HUD & check win condition
        syncHUD(elapsedMs);
        hudState.recordClear(spin, lines, eventNow);

        if (linesCleared >= targetLines) {
            session.clearGame();
        }
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
        double levelMult = getLevelMultiplier(level);

        double total = BASE_CLEAR_POINTS
                + (linePoints + spinPoints) * spinMult * b2bMult * comboMult * levelMult;

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
        if (currentCombo < COMBO_START_COUNT) return 1.0;
        int capped = Math.min(currentCombo, COMBO_CAP_COUNT);
        double progress = (double) (capped - (COMBO_START_COUNT - 1))
                / (COMBO_CAP_COUNT - (COMBO_START_COUNT - 1));
        return 1.0 + (MAX_COMBO_MULTIPLIER - 1.0) * progress;
    }

    private double getLevelMultiplier(int currentLevel) {
        int capped = Math.clamp(currentLevel, 1, MULTIPLIER_CAP_LEVEL);
        double progress = (capped - 1.0) / (MULTIPLIER_CAP_LEVEL - 1.0);
        double p2 = progress * progress;
        return 1.0 + (MAX_LEVEL_MULTIPLIER - 1.0) * (p2 * p2); // p^4 (use p2 * progress for p^3)
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

    private void syncHUD(long elapsedMs) {
        hudState.updateStats(level, targetLines, "", elapsedMs);
        hudState.setLinesCleared(linesCleared);
        hudState.setScore(score);
        hudState.setCombo(combo);
        hudState.setB2bStreak(b2bActive ? b2bStreak : 0);
    }

    @Override
    public boolean supportsPerfectClear() {
        return super.supportsPerfectClear();
    }

    @Override
    public GameResult getResult() {
        return new GameResult(String.format("%,d", score), score);
    }

    public int getLinesCleared() { return linesCleared; }
    public int getTargetLines()  { return targetLines; }
    public int getLevel()        { return level; }
    public long getScore()       { return score; }
    public int getCombo()        { return combo; }
    public int getB2bStreak()    { return b2bActive ? b2bStreak : 0; }
}