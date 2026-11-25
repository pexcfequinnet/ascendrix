package org.example.ascendrix.model.mode;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.board.FadeMap;
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
import org.example.ascendrix.model.mode.shared.RollPhase;
import org.example.ascendrix.model.piece.TetrominoQueue;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.state.GameSession.GameResult;

public class OverdriveRuleset extends BaseModeRuleset {

    public static final int MAX_LEVEL = 2000;
    private static final int MONOCHROME_START_LEVEL = 1000;
    private static final int FADING_START_LEVEL     = 1500;

    private static final int DEFAULT_LOCK_RESET_LIMIT = 15;
    private static final int ROLL_LOCK_RESET_LIMIT    = 8;

    // Torikan time gates (evaluated when crossing 499 -> 500 and 999 -> 1000)
    private static final int TORIKAN_GATE_1        = 500;
    private static final int TORIKAN_GATE_2        = 1000;
    private static final long TIME_LIMIT_500_MS    = 180_000L; // 3:00 at Lv 500 (end of 4e)
    private static final long TIME_LIMIT_1000_MS   = 360_000L; // 6:00 at Lv 1000 (end of 9e)

    private static final long ROLL_DURATION_NS           = 60_000_000_000L;
    private static final long REGRET_DISPLAY_DURATION_NS = 3_000_000_000L;
    private static final long REGRET_FADE_WINDOW_NS      = 500_000_000L;

    private final HandlingConfig handlingConfig;
    private final OverdriveGradeTracker gradeTracker = new OverdriveGradeTracker();
    private final OverdriveSectionTracker sectionTracker = new OverdriveSectionTracker();
    private final GarbageSpawner garbageSpawner = new GarbageSpawner();
    private final FadeMap fadeMap = new FadeMap();

    private int level = 0;
    private int lastSection = 0;
    private int sectionStartLevel = 0;
    private double sectionStartTime = 0.0;

    private boolean fadingActivated = false;
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

        if (rollTriggered && fadeMap.isFadingBoard()) {
            // Start the 60s survival clock and Roll garbage only after the board wipe finishes
            rollStartTime = now;
            syncHUDState(now, elapsedMs);
            return;
        }

        checkRollTimeout(now, session);
        garbageSpawner.update(now, ModeTables.getOverdriveGarbageInterval(level), session);

        syncHUDState(now, elapsedMs);
    }

    @Override
    public void onPieceSpawned(GameSession session) {
        if (rollTriggered || timeLimitTriggered) return;

        long eventNow = currentNow();
        if (level < MAX_LEVEL - 1 && level % 100 != 99) {
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

        // Track lock timestamps during Fading (1500-1999) and Invisible Roll (2000)
        if ((fadingActivated || rollTriggered) && session.getLastLockedBlocks() != null) {
            fadeMap.add(
                    session.getLastLockedBlocks(),
                    session.getLastLockedX(),
                    session.getLastLockedY(),
                    eventNow
            );
        }

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

            // Evaluate Torikan gate when crossing 499 -> 500 or 999 -> 1000
            if (checkTorikanGate(previousLevel, elapsedMs, session)) {
                double elapsedSeconds = session.getTimer().getElapsedSeconds(eventNow);
                checkSectionTransition(elapsedSeconds, eventNow);
                syncHUDState(eventNow, elapsedMs);
                return;
            }

            double elapsedSeconds = session.getTimer().getElapsedSeconds(eventNow);
            checkSectionTransition(elapsedSeconds, eventNow);
            checkRollTransition(session, eventNow);
        }

        syncHUDState(eventNow, elapsedMs);
    }

    /* ========================== SECTIONS, PHASES & ROLL ========================== */

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
        return false;
    }

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
        updatePhaseModifiers(now);
        updateAllPhysics(level);
    }

    private void updatePhaseModifiers(long now) {
        if (!rollTriggered && level >= FADING_START_LEVEL && level < MAX_LEVEL) {
            ModeTables.FadeEntry fade = ModeTables.getOverdriveFade(level);
            if (!fadingActivated) {
                fadingActivated = true;
                fadeMap.activateFading(now, fade.delayNs(), fade.durationNs());
            } else {
                fadeMap.setFadingTimingNs(fade.delayNs(), fade.durationNs());
            }
        }
    }

    private void checkRollTransition(GameSession session, long now) {
        if (rollTriggered || timeLimitTriggered || level < MAX_LEVEL) return;

        rollTriggered = true;
        rollStartTime = now;
        session.getTimer().pause();
        garbageSpawner.reset();

        fadeMap.triggerBoardFade(Board.ROWS, RollPhase.INVISIBLE, now);
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
            fadeMap.resetFadeStatus();
            syncHUDState(now, session.getTimer().getElapsedMs(now));
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

    /* ========================== CAPABILITY FLAGS ========================== */

    @Override
    public boolean isRollActive() {
        return rollTriggered;
    }

    @Override
    public boolean isDecolorActive() {
        // Monochrome begins at 1000 and stays active through Fading (1500+) and Last Roll (2000)
        return level >= MONOCHROME_START_LEVEL;
    }

    @Override
    public boolean isInvisible() {
        return rollTriggered;
    }

    @Override
    public FadeMap getFadeMap() {
        return (fadingActivated || rollTriggered) ? fadeMap : null;
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
    public GameResult getResult() {
        long sortValue = (gradeTracker.getSortRank() * 10_000L) + this.level;
        return new GameResult(gradeTracker.getDisplayGrade(), sortValue);
    }

    @Override
    public void debugSetLevel(int targetLevel) {
        long now = currentNow();
        level = Math.clamp(targetLevel, 0, MAX_LEVEL);
        sectionStartLevel = (level / 100) * 100;
        lastSection = level / 100;
        sectionStartTime = 0.0;

        fadingActivated = false;
        timeLimitTriggered = false;
        rollTriggered = false;
        rollStartTime = -1L;

        fadeMap.resetFadeStatus();
        sectionTracker.reset();
        garbageSpawner.reset();

        if (level >= MAX_LEVEL) {
            rollTriggered = true;
            rollStartTime = now;
            fadeMap.triggerBoardFade(Board.ROWS, RollPhase.INVISIBLE, now);
        } else {
            updatePhaseModifiers(now);
        }

        updateAllPhysics(level);
        syncHUDState(now, 0L);
    }

    public int getLevel()                        { return level; }
    public int getSectionStartLevel()            { return sectionStartLevel; }
    public OverdriveGradeTracker getGradeTracker() { return gradeTracker; }
}