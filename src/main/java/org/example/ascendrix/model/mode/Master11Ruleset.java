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
import org.example.ascendrix.model.mode.shared.GarbageSpawner;
import org.example.ascendrix.model.mode.shared.Master11Grade;
import org.example.ascendrix.model.mode.shared.Master11GradeTracker;
import org.example.ascendrix.model.mode.shared.ModeTables;
import org.example.ascendrix.model.piece.TetrominoQueue;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.state.GameSession.GameResult;

public class Master11Ruleset extends BaseModeRuleset {

    public static final int MAX_LEVEL = 1500;
    private static final int MONOCHROME_START_LEVEL = 1000;

    private static final int DEFAULT_LOCK_RESET_LIMIT = 15;
    private static final int ROLL_LOCK_RESET_LIMIT    = 8;

    // Torikan checkpoints (500, 1000, 1300)
    private static final int TORIKAN_GATE_1        = 500;
    private static final int TORIKAN_GATE_2        = 1000;
    private static final int TORIKAN_GATE_3        = 1300;
    private static final long TIME_LIMIT_500_MS    = 210_000L; // 3:30
    private static final long TIME_LIMIT_1000_MS   = 390_000L; // 6:30
    private static final long TIME_LIMIT_1300_MS   = 510_000L; // 8:30

    private static final long ROLL_DURATION_NS           = 60_000_000_000L;
    private static final long REGRET_DISPLAY_DURATION_NS = 3_000_000_000L;
    private static final long REGRET_FADE_WINDOW_NS      = 500_000_000L;

    private final HandlingConfig handlingConfig;
    private final Master11GradeTracker gradeTracker = new Master11GradeTracker();
    private final GarbageSpawner garbageSpawner = new GarbageSpawner();

    private int currentSpdLv = 1;
    private int level = 0;
    private int lastSection = 0;
    private long sectionStartTimeNs = 0L;

    private boolean timeLimitTriggered = false;
    private boolean rollTriggered = false;
    private long rollStartTime = -1L;
    private long regretDisplayTime = -1L;
    private long lastNowNs = 0L;

    private Master11Ruleset(HandlingConfig handlingConfig,
                            MovementProcessor handling,
                            GravityModel gravity,
                            LockDelay lockDelay,
                            ARETimer are,
                            RotationSystem rotationSystem) {
        super(GameMode.MASTER_11, handling, gravity, lockDelay, are, rotationSystem);
        this.handlingConfig = handlingConfig;
        applySpeedTierPhysics(1);
        syncHUDState(0L, 0L);
    }

    public static Master11Ruleset create() {
        HandlingConfig config = HandlingConfig.defaultConfig();
        ARETimer are = new ARETimer(
                ModeTables.getMaster11SpawnDelay(1),
                ModeTables.getMaster11LineClearDelay(1),
                ModeTables.getMaster11ClearAnimDelay(1)
        );
        GravityModel gravity = new GravityModel(20.0);
        LockDelayTimer lockDelay = new LockDelayTimer(
                ModeTables.getMaster11LockDelay(1),
                DEFAULT_LOCK_RESET_LIMIT
        );

        return new Master11Ruleset(
                config,
                new MovementProcessor(config),
                gravity,
                lockDelay,
                are,
                new SRS()
        );
    }

    /* ========================== LIFECYCLE HOOKS ========================== */

    @Override
    public void update(long now, GameSession session) {
        this.lastNowNs = now;
        long elapsedMs = session.getTimer().getElapsedMs(now);

        checkRollTimeout(now, session);
        long garbageInterval = rollTriggered
                ? GarbageSpawner.PAUSED
                : ModeTables.getMaster11GarbageInterval(level);
        garbageSpawner.update(now, garbageInterval, session);

        syncHUDState(now, elapsedMs);
    }

    @Override
    public void onPieceSpawned(GameSession session) {
        if (rollTriggered || timeLimitTriggered) return;

        long eventNow = currentNow();
        if (level < MAX_LEVEL - 1 && level % 100 != 99) {
            level++;
        }

        long elapsedNs = session.getTimer().getElapsedNs(eventNow);
        checkSectionTransition(elapsedNs, eventNow);
        ModeTables.applyMaster11ARE(are, currentSpdLv);
        lockDelay.setLockNs(ModeTables.getMaster11LockDelay(currentSpdLv));
        syncHUDState(eventNow, session.getTimer().getElapsedMs(eventNow));
    }

    @Override
    public void onPiecePlaced(GameSession session) {
        long eventNow = currentNow();
        long elapsedNs = session.getTimer().getElapsedNs(eventNow);
        checkSectionTransition(elapsedNs, eventNow);
    }

    @Override
    public void onLinesCleared(int lines,
                               SpinType spin,
                               int dropRows,
                               TetrominoQueue.DropType dropType,
                               GameSession session) {
        long eventNow = currentNow();
        hudState.recordClear(spin, lines, eventNow);

        long elapsedMs = session.getTimer().getElapsedMs(eventNow);

        if (!rollTriggered && !timeLimitTriggered && lines > 0) {
            int previousLevel = level;
            switch (lines) {
                case 1 -> level = Math.min(level + 1, MAX_LEVEL);
                case 2 -> level = Math.min(level + 2, MAX_LEVEL);
                case 3 -> level = Math.min(level + 3, MAX_LEVEL);
                case 4 -> level = Math.min(level + 6, MAX_LEVEL);
                default -> {}
            }

            if (checkTorikanGate(previousLevel, elapsedMs, session)) {
                long elapsedNs = session.getTimer().getElapsedNs(eventNow);
                checkSectionTransition(elapsedNs, eventNow);
                syncHUDState(eventNow, elapsedMs);
                return;
            }

            long elapsedNs = session.getTimer().getElapsedNs(eventNow);
            checkSectionTransition(elapsedNs, eventNow);
            checkRollTransition(session, eventNow);
        }

        syncHUDState(eventNow, elapsedMs);
    }

    /* ========================== SECTIONS, CHECKPOINTS & ROLL ========================== */

    private boolean checkTorikanGate(int previousLevel, long elapsedMs, GameSession session) {
        if (previousLevel < TORIKAN_GATE_1 && level >= TORIKAN_GATE_1 && elapsedMs > TIME_LIMIT_500_MS) {
            level = TORIKAN_GATE_1;
            timeLimitTriggered = true;
            session.clearGame();
            return true;
        }
        if (previousLevel < TORIKAN_GATE_2 && level >= TORIKAN_GATE_2 && elapsedMs > TIME_LIMIT_1000_MS) {
            level = TORIKAN_GATE_2;
            timeLimitTriggered = true;
            session.clearGame();
            return true;
        }
        if (previousLevel < TORIKAN_GATE_3 && level >= TORIKAN_GATE_3 && elapsedMs > TIME_LIMIT_1300_MS) {
            level = TORIKAN_GATE_3;
            timeLimitTriggered = true;
            session.clearGame();
            return true;
        }
        return false;
    }

    private void checkSectionTransition(long elapsedNs, long now) {
        int currentSection = level / 100;
        if (currentSection <= lastSection) return;

        while (lastSection < currentSection) {
            int completedSectionStart = lastSection * 100;
            long sectionDurationNs = Math.max(0L, elapsedNs - sectionStartTimeNs);

            boolean regret = gradeTracker.onSectionCompleted(completedSectionStart, sectionDurationNs);
            if (regret) {
                regretDisplayTime = now;
            }

            sectionStartTimeNs = elapsedNs;
            lastSection++;
        }

        int targetSpdLv = ModeTables.getMaster11SpeedLevel(level);
        if (targetSpdLv != currentSpdLv) {
            currentSpdLv = targetSpdLv;
            applySpeedTierPhysics(currentSpdLv);
        }
    }

    private void checkRollTransition(GameSession session, long now) {
        if (rollTriggered || timeLimitTriggered || level < MAX_LEVEL) return;

        rollTriggered = true;
        rollStartTime = now;
        session.getTimer().pause();
        garbageSpawner.reset();

        currentSpdLv = ModeTables.getMaster11SpeedLevel(MAX_LEVEL);
        applySpeedTierPhysics(currentSpdLv);
    }

    private void checkRollTimeout(long now, GameSession session) {
        if (!rollTriggered) return;
        if (rollStartTime < 0L) {
            rollStartTime = now;
        }
        if (now - rollStartTime >= ROLL_DURATION_NS) {
            gradeTracker.onCreditRollCleared();
            syncHUDState(now, session.getTimer().getElapsedMs(now));
            session.clearGame();
        }
    }

    private void applySpeedTierPhysics(int speedLevel) {
        ModeTables.applyMaster11Gravity(gravity, level);
        ModeTables.applyMaster11ARE(are, speedLevel);
        ModeTables.applyMaster11Handling(handlingConfig, speedLevel);
        lockDelay.setLockNs(ModeTables.getMaster11LockDelay(speedLevel));
        lockDelay.setLockResetLimit(rollTriggered ? ROLL_LOCK_RESET_LIMIT : DEFAULT_LOCK_RESET_LIMIT);
    }

    /* ========================== CAPABILITY FLAGS ========================== */

    @Override
    public boolean isRollActive() {
        return rollTriggered;
    }

    @Override
    public boolean isDecolorActive() {
        return level >= MONOCHROME_START_LEVEL && level < MAX_LEVEL;
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public boolean supportsPerfectClear() {
        return false;
    }

    /* ========================== HUD & RESULT ========================== */

    private void syncHUDState(long now, long elapsedMs) {
        int displayLevel = Math.min(level, MAX_LEVEL);
        int sectionStart = (displayLevel / 100) * 100;
        int sectionEnd = Math.min(sectionStart + 100, MAX_LEVEL);

        hudState.updateStats(displayLevel, sectionEnd, gradeTracker.getDisplayGrade(), elapsedMs);
        hudState.setRollActive(rollTriggered);
        hudState.setRollCleared(gradeTracker.getCurrentGrade().isGrandMaster());
        hudState.setRegretAlpha(computeRegretAlpha(now));
    }

    private double computeRegretAlpha(long nowNs) {
        if (regretDisplayTime < 0L || nowNs <= 0L) return 0.0;
        long elapsed = nowNs - regretDisplayTime;
        if (elapsed < 0L || elapsed >= REGRET_DISPLAY_DURATION_NS) return 0.0;

        long fadeStart = REGRET_DISPLAY_DURATION_NS - REGRET_FADE_WINDOW_NS;
        if (elapsed > fadeStart) {
            return 1.0 - (double) (elapsed - fadeStart) / REGRET_FADE_WINDOW_NS;
        }
        return 1.0;
    }

    private long currentNow() {
        return (lastNowNs > 0L) ? lastNowNs : System.nanoTime();
    }

    @Override
    public GameResult getResult() {
        Master11Grade grade = gradeTracker.getCurrentGrade();
        long sortValue = (grade.ordinal() * 10_000L) + this.level;
        return new GameResult(gradeTracker.getDisplayGrade(), sortValue);
    }

    @Override
    public void debugSetLevel(int targetLevel) {
        long now = currentNow();
        level = Math.clamp(targetLevel, 0, MAX_LEVEL);
        lastSection = level / 100;
        sectionStartTimeNs = 0L;

        timeLimitTriggered = false;
        rollTriggered = false;
        rollStartTime = -1L;

        gradeTracker.reset();
        garbageSpawner.reset();

        if (level >= MAX_LEVEL) {
            rollTriggered = true;
            rollStartTime = now;
        }

        currentSpdLv = ModeTables.getMaster11SpeedLevel(level);
        applySpeedTierPhysics(currentSpdLv);
        syncHUDState(now, 0L);
    }

    public int getLevel()                         { return level; }
    public int getCurrentSpeedLevel()             { return currentSpdLv; }
    public Master11GradeTracker getGradeTracker() { return gradeTracker; }
}