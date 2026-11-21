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
import org.example.ascendrix.model.mode.shared.ModeTables;
import org.example.ascendrix.model.mode.shared.OverdriveGradeTracker;
import org.example.ascendrix.model.mode.shared.OverdriveSectionTracker;
import org.example.ascendrix.model.piece.TetrominoQueue;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.state.GameSession.GameResult;

public class OverdriveRuleset extends BaseModeRuleset {

    public static final int MAX_LEVEL = 1500;
    private static final int DEFAULT_LOCK_RESET_LIMIT = 15;
    private static final int ROLL_LOCK_RESET_LIMIT = 8;

    private static final long TIME_LIMIT_500_MS  = 180_000L; // 3:00 in ms
    private static final long TIME_LIMIT_1000_MS = 360_000L; // 6:00 in ms

    private static final long ROLL_DURATION_NS           = 60_000_000_000L;
    private static final long REGRET_DISPLAY_DURATION_NS = 3_000_000_000L;
    private static final long REGRET_FADE_WINDOW_NS      = 500_000_000L;

    private final HandlingConfig handlingConfig;
    private final OverdriveGradeTracker gradeTracker = new OverdriveGradeTracker();
    private final OverdriveSectionTracker sectionTracker = new OverdriveSectionTracker();
    private final GarbageSpawner garbageSpawner = new GarbageSpawner();

    private int level = 0;
    private int lastSection = 0;
    private int sectionStartLevel = 0;
    private double sectionStartTime = 0.0;

    private boolean timeLimitTriggered = false;
    private boolean rollTriggered = false;
    private long rollStartTime = -1L;
    private long regretDisplayTime = -1L;
    private long lastNowNs = 0L;

    private OverdriveRuleset(HandlingConfig handlingConfig,
                             MovementProcessor handling,
                             GravityModel gravity,
                             LockDelay lockDelay,
                             ARETimer are,
                             RotationSystem rotationSystem) {
        super(GameMode.OVERDRIVE, handling, gravity, lockDelay, are, rotationSystem);
        this.handlingConfig = handlingConfig;
        updateAllPhysics(0);
        syncHUDState(0L, 0L);
    }

    public static OverdriveRuleset create() {
        HandlingConfig config = HandlingConfig.defaultConfig();
        ARETimer are = new ARETimer(
                ModeTables.getOverdriveSpawnDelay(0),
                ModeTables.getOverdriveLineClearDelay(0),
                ModeTables.getOverdriveClearAnimDelay(0)
        );
        GravityModel gravity = new GravityModel(20.0);
        LockDelayTimer lockDelay = new LockDelayTimer(
                ModeTables.getOverdriveLockDelay(0),
                DEFAULT_LOCK_RESET_LIMIT
        );

        return new OverdriveRuleset(
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

        checkTimeLimit(elapsedMs, session);
        checkRollTimeout(now, session);
        garbageSpawner.update(now, level, session);

        syncHUDState(now, elapsedMs);
    }

    @Override
    public void onPieceSpawned(GameSession session) {
        if (rollTriggered) return;

        long eventNow = currentNow();
        if (level < 1499 && level % 100 != 99) {
            level++;
        }

        double elapsedSeconds = session.getTimer().getElapsedSeconds(eventNow);
        checkSectionTransition(elapsedSeconds, eventNow);
        ModeTables.applyOverdriveARE(are, level);
        lockDelay.setLockNs(ModeTables.getOverdriveLockDelay(level));
        syncHUDState(eventNow, session.getTimer().getElapsedMs(eventNow));
    }

    @Override
    public void onPiecePlaced(GameSession session) {
        long eventNow = currentNow();
        double elapsedSeconds = session.getTimer().getElapsedSeconds(eventNow);
        checkSectionTransition(elapsedSeconds, eventNow);
    }

    @Override
    public void onLinesCleared(int lines,
                               SpinType spin,
                               int dropRows,
                               TetrominoQueue.DropType dropType,
                               GameSession session) {
        long eventNow = currentNow();
        hudState.recordClear(spin, lines, eventNow);

        if (!rollTriggered && lines > 0) {
            switch (lines) {
                case 1 -> level = Math.min(level + 1, MAX_LEVEL);
                case 2 -> level = Math.min(level + 2, MAX_LEVEL);
                case 3 -> level = Math.min(level + 3, MAX_LEVEL);
                case 4 -> level = Math.min(level + 6, MAX_LEVEL);
                default -> {}
            }
            double elapsedSeconds = session.getTimer().getElapsedSeconds(eventNow);
            checkSectionTransition(elapsedSeconds, eventNow);
            checkRollTransition(session, eventNow);
        }

        syncHUDState(eventNow, session.getTimer().getElapsedMs(eventNow));
    }

    /* ========================== SECTIONS, ROLL & TIME GATES ========================== */

    private void checkSectionTransition(double elapsedSeconds, long now) {
        int currentSection = level / 100;
        if (currentSection <= lastSection) return;

        double sectionTime = elapsedSeconds - sectionStartTime;
        int completedIdx = sectionTracker.getSectionIndex(lastSection * 100);

        if (completedIdx != -1) {
            OverdriveSectionTracker.SectionResult result =
                    sectionTracker.evaluate(completedIdx, sectionTime, gradeTracker);
            if (result == OverdriveSectionTracker.SectionResult.REGRET) {
                regretDisplayTime = now;
            }
        }

        lastSection = currentSection;
        sectionStartLevel = currentSection * 100;
        sectionStartTime = elapsedSeconds;
        updateAllPhysics(level);
    }

    private void checkRollTransition(GameSession session, long now) {
        if (rollTriggered || level < MAX_LEVEL) return;

        rollTriggered = true;
        rollStartTime = now;
        session.getTimer().pause();

        updateAllPhysics(MAX_LEVEL);
        lockDelay.setLockResetLimit(ROLL_LOCK_RESET_LIMIT);
    }

    private void checkRollTimeout(long now, GameSession session) {
        if (!rollTriggered) return;
        if (rollStartTime < 0L) {
            rollStartTime = now;
        }
        if (now - rollStartTime >= ROLL_DURATION_NS) {
            gradeTracker.onRollCleared();
            syncHUDState(now, session.getTimer().getElapsedMs(now));
            session.clearGame();
        }
    }

    private void checkTimeLimit(long elapsedMs, GameSession session) {
        if (rollTriggered || timeLimitTriggered) return;

        if (level >= 1000 && elapsedMs > TIME_LIMIT_1000_MS) {
            timeLimitTriggered = true;
            session.clearGame();
        } else if (level >= 500 && elapsedMs > TIME_LIMIT_500_MS) {
            timeLimitTriggered = true;
            session.clearGame();
        }
    }

    private void updateAllPhysics(int targetLevel) {
        ModeTables.applyOverdriveGravity(gravity, targetLevel);
        ModeTables.applyOverdriveARE(are, targetLevel);
        ModeTables.applyOverdriveHandling(handlingConfig, targetLevel);
        lockDelay.setLockNs(ModeTables.getOverdriveLockDelay(targetLevel));
        lockDelay.setLockResetLimit(rollTriggered ? ROLL_LOCK_RESET_LIMIT : DEFAULT_LOCK_RESET_LIMIT);
    }

    /* ========================== HUD & RESULT ========================== */

    private void syncHUDState(long now, long elapsedMs) {
        int displayLevel = Math.min(level, MAX_LEVEL);
        int sectionStart = (displayLevel / 100) * 100;
        int sectionEnd = Math.min(sectionStart + 100, MAX_LEVEL);

        hudState.updateStats(displayLevel, sectionEnd, gradeTracker.getDisplayGrade(), elapsedMs);
        hudState.setRollActive(rollTriggered);
        hudState.setRollCleared(gradeTracker.isRollCleared());
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
    public boolean isRollActive() {
        return rollTriggered;
    }

    @Override
    public boolean isDecolorActive() {
        return level >= 1000;
    }

    @Override
    public boolean supportsPerfectClear() {
        return false;
    }

    @Override
    public GameResult getResult() {
        long sortValue = (gradeTracker.getSortRank() * 10_000L) + this.level;
        return new GameResult(gradeTracker.getDisplayGrade(), sortValue);
    }

    @Override
    public void debugSetLevel(int targetLevel) {
        level = Math.clamp(targetLevel, 0, MAX_LEVEL);
        sectionStartLevel = (level / 100) * 100;
        lastSection = level / 100;
        sectionStartTime = 0.0;
        updateAllPhysics(level);
        garbageSpawner.reset();
        syncHUDState(currentNow(), 0L);
    }

    public int getLevel()                        { return level; }
    public int getSectionStartLevel()            { return sectionStartLevel; }
    public OverdriveGradeTracker getGradeTracker() { return gradeTracker; }
}