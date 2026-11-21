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
import org.example.ascendrix.model.mode.shared.GradeTracker;
import org.example.ascendrix.model.mode.shared.MasterGrade;
import org.example.ascendrix.model.mode.shared.ModeTables;
import org.example.ascendrix.model.mode.shared.RollGrade;
import org.example.ascendrix.model.mode.shared.RollPhase;
import org.example.ascendrix.model.mode.shared.SectionTracker;
import org.example.ascendrix.model.piece.TetrominoQueue;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.state.GameSession.GameResult;

public class MasterRuleset extends BaseModeRuleset {

    public static final int MAX_LEVEL = 999;
    private static final int LOCK_RESET_LIMIT = 15;
    private static final long ROLL_DURATION_NS = 60_000_000_000L;
    private static final long BANNER_DISPLAY_DURATION_NS = 3_000_000_000L;
    private static final long BANNER_FADE_WINDOW_NS = 500_000_000L;

    private final HandlingConfig handlingConfig;
    private final GradeTracker gradeTracker = new GradeTracker();
    private final SectionTracker sectionTracker = new SectionTracker();
    private final FadeMap fadeMap = new FadeMap();

    private int currentSpdLv = 1;
    private int level = 0;
    private int lastSection = 0;
    private int speedLevelStartLevel = 0;
    private int sectionStartLevel = 0;

    private boolean gravityAtFloor = false;   // true = locked at section bottom (300)
    private boolean gravityAtCeiling = false; // true = locked at section top (0)

    // COOL / REGRET banner timers
    private int lastEvaluatedSection = -1;
    private long coolDisplayTime = -1L;
    private long regretDisplayTime = -1L;

    // Roll state
    private RollPhase masterRollPhase = RollPhase.NORMAL;
    private long rollStartTime = -1L;
    private boolean rollTriggered = false;
    private long lastNowNs = 0L;

    private MasterRuleset(HandlingConfig handlingConfig,
                          MovementProcessor handling,
                          GravityModel gravity,
                          LockDelay lockDelay,
                          ARETimer are,
                          RotationSystem rotationSystem) {
        super(GameMode.MASTER, handling, gravity, lockDelay, are, rotationSystem);
        this.handlingConfig = handlingConfig;
        applySpeedLevelPhysics(1, 0);
        syncHUDState(0L, 0L);
    }

    public static MasterRuleset create() {
        HandlingConfig config = HandlingConfig.defaultConfig();
        ARETimer are = new ARETimer(
                ModeTables.getMasterSpawnDelay(1),
                ModeTables.getMasterLineClearDelay(1),
                ModeTables.getMasterClearAnimDelay(1)
        );
        GravityModel gravity = new GravityModel(1.0 / 60.0);
        LockDelayTimer lockDelay = new LockDelayTimer(getLockDelayForSpeedLevel(1), LOCK_RESET_LIMIT);

        return new MasterRuleset(
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
        checkRollTimeout(now, session);

        long elapsedMs = session.getTimer().getElapsedMs(now);
        syncHUDState(now, elapsedMs);
    }

    @Override
    public void onPieceSpawned(GameSession session) {
        long eventNow = currentNow();
        int sectionCap = (level >= 900) ? 98 : 99;
        if (level < MAX_LEVEL && level % 100 != sectionCap) {
            level++;
        }

        double elapsedSeconds = session.getTimer().getElapsedSeconds(eventNow);
        checkSectionTransition(elapsedSeconds, eventNow);
        ModeTables.applyMasterGravity(gravity, currentSpdLv, level - sectionStartLevel);
        lockDelay.setLockNs(getLockDelayForSpeedLevel(currentSpdLv));
        lockDelay.setLockResetLimit(LOCK_RESET_LIMIT);
    }

    @Override
    public void onPiecePlaced(GameSession session) {
        long eventNow = currentNow();
        if (masterRollPhase == RollPhase.FADING && session.getLastLockedBlocks() != null) {
            fadeMap.add(
                    session.getLastLockedBlocks(),
                    session.getLastLockedX(),
                    session.getLastLockedY(),
                    eventNow
            );
        }

        double elapsedSeconds = session.getTimer().getElapsedSeconds(eventNow);
        checkSectionTransition(elapsedSeconds, eventNow);
        lockDelay.setLockNs(getLockDelayForSpeedLevel(currentSpdLv));
        lockDelay.setLockResetLimit(LOCK_RESET_LIMIT);

        int pos;
        if (gravityAtFloor)        pos = 300;
        else if (gravityAtCeiling) pos = 0;
        else                       pos = Math.min(level - speedLevelStartLevel, 299);

        ModeTables.applyMasterGravity(gravity, currentSpdLv, pos);
    }

    @Override
    public void onLinesCleared(int lines,
                               SpinType spin,
                               int dropRows,
                               TetrominoQueue.DropType dropType,
                               GameSession session) {
        long eventNow = currentNow();
        hudState.recordClear(spin, lines, eventNow);

        switch (lines) {
            case 1 -> level = Math.min(level + 1, MAX_LEVEL);
            case 2 -> level = Math.min(level + 2, MAX_LEVEL);
            case 3 -> level = Math.min(level + 3, MAX_LEVEL);
            case 4 -> level = Math.min(level + 6, MAX_LEVEL);
            default -> {}
        }

        double elapsedSeconds = session.getTimer().getElapsedSeconds(eventNow);
        checkSectionTransition(elapsedSeconds, eventNow);

        if (lines <= 0) {
            gradeTracker.resetCombo();
            syncHUDState(eventNow, session.getTimer().getElapsedMs(eventNow));
            return;
        }

        checkRollTransition(session, elapsedSeconds, eventNow);

        int tier = Math.clamp(currentSpdLv, 1, 4);
        gradeTracker.decay(tier, masterRollPhase);
        gradeTracker.calculate(lines, spin, tier, masterRollPhase);

        syncHUDState(eventNow, session.getTimer().getElapsedMs(eventNow));
    }

    /* ========================== SECTIONS & SPEED LEVELS ========================== */

    private void checkSectionTransition(double elapsedSeconds, long now) {
        int currentSection = level / 100;

        // Evaluate COOL / REGRET at x80 mark once per section
        if (level % 100 >= 80 && lastEvaluatedSection < currentSection) {
            lastEvaluatedSection = currentSection;
            SectionTracker.SectionResult result = sectionTracker.evaluateSection(currentSection, elapsedSeconds);

            if (result == SectionTracker.SectionResult.COOL) {
                coolDisplayTime = now;
                if (gravityAtFloor) {
                    speedLevelStartLevel = level;
                    gravityAtFloor = false;
                    gravityAtCeiling = true;
                }
            } else if (result == SectionTracker.SectionResult.REGRET) {
                regretDisplayTime = now;
                gradeTracker.applyRegret();
                currentSpdLv = Math.max(1, currentSpdLv - 1);
                speedLevelStartLevel = level;
                gravityAtFloor = true;
                gravityAtCeiling = false;
            }
        }

        // Section boundary transition
        if (currentSection > lastSection) {
            lastSection = currentSection;
            sectionStartLevel = currentSection * 100;
            updateSpeedLevel();
            ModeTables.applyMasterGravity(gravity, currentSpdLv, level - sectionStartLevel);
        }
    }

    private void updateSpeedLevel() {
        int newSpdLv = calculateSpeedLevel();
        if (newSpdLv == currentSpdLv) return;

        currentSpdLv = newSpdLv;
        speedLevelStartLevel = level;
        applySpeedLevelPhysics(currentSpdLv, level - sectionStartLevel);
    }

    private void applySpeedLevelPhysics(int speedLevel, int levelWithinSection) {
        lockDelay.setLockNs(getLockDelayForSpeedLevel(speedLevel));
        lockDelay.setLockResetLimit(LOCK_RESET_LIMIT);

        are.clearOverrides();
        ModeTables.applyMasterARE(are, speedLevel);
        ModeTables.applyMasterHandling(handlingConfig, speedLevel);
        ModeTables.applyMasterGravity(gravity, speedLevel, levelWithinSection);

        if (speedLevel == 2 && sectionTracker.missedCools() >= 2) {
            are.setOverride(
                    2,
                    ModeTables.getMasterSpawnDelay(2),
                    ModeTables.getMasterLineClearDelay(2)
            );
            are.applySpeedLevel(2);
        }
    }

    private int calculateSpeedLevel() {
        int cools = sectionTracker.getCoolCount();

        // Cool-triggered early transitions take priority
        if (cools >= 6 && level >= 600) return 4;
        if (cools >= 4 && level >= 400) return 3;
        if (cools >= 2 && level >= 300) return 2;

        // Default level transitions
        if (level >= 900) return 4;
        if (level >= 700) return 3;
        if (level >= 500) return 2;

        return 1;
    }

    private static long getLockDelayForSpeedLevel(int speedLevel) {
        return switch (speedLevel) {
            case 2 -> 900_000_000L;
            case 3 -> 600_000_000L;
            case 4 -> 350_000_000L;
            default -> 1_150_000_000L;
        };
    }

    /* ========================== ROLL PHASE ========================== */

    private void checkRollTransition(GameSession session, double elapsedSeconds, long now) {
        if (rollTriggered || level < MAX_LEVEL) return;

        rollTriggered = true;
        rollStartTime = now;

        RollPhase newPhase = sectionTracker.meetsInvisibleRequirements(
                elapsedSeconds,
                gradeTracker.getCurrentGrade()
        ) ? RollPhase.INVISIBLE : RollPhase.FADING;

        session.getTimer().pause();
        masterRollPhase = newPhase;
        gradeTracker.setPhase(newPhase, 0.0);
        fadeMap.triggerBoardFade(Board.ROWS, newPhase, now);
    }

    private void checkRollTimeout(long now, GameSession session) {
        if (!rollTriggered || masterRollPhase == RollPhase.NORMAL) return;
        if (now - rollStartTime >= ROLL_DURATION_NS) {
            onRollComplete(session);
        }
    }

    private void onRollComplete(GameSession session) {
        double overflow = gradeTracker.getGradeOverflow();
        if (masterRollPhase == RollPhase.INVISIBLE) {
            gradeTracker.addGradeValue(1.5 + overflow);
        } else {
            gradeTracker.addGradeValue(0.75 + overflow);
        }
        fadeMap.resetFadeStatus();
        session.clearGame();
    }

    /* ========================== HUD & RESULT ========================== */

    private void syncHUDState(long now, long elapsedMs) {
        int displayLevel = Math.min(level, MAX_LEVEL);
        int sectionStart = (displayLevel / 100) * 100;
        int sectionEnd = Math.min(sectionStart + 100, MAX_LEVEL);

        hudState.updateStats(displayLevel, sectionEnd, gradeTracker.getDisplayGrade(), elapsedMs);
        hudState.setGradeProgress(gradeTracker.getProgressToNextGrade());
        hudState.setCombo(gradeTracker.getCombo());
        hudState.setRollActive(masterRollPhase != RollPhase.NORMAL);
        hudState.setCoolAlpha(computeBannerAlpha(coolDisplayTime, now));
        hudState.setRegretAlpha(computeBannerAlpha(regretDisplayTime, now));
    }

    private double computeBannerAlpha(long startTimeNs, long nowNs) {
        if (startTimeNs < 0L || nowNs <= 0L) return 0.0;
        long elapsed = nowNs - startTimeNs;
        if (elapsed < 0L || elapsed >= BANNER_DISPLAY_DURATION_NS) return 0.0;

        long fadeStart = BANNER_DISPLAY_DURATION_NS - BANNER_FADE_WINDOW_NS;
        if (elapsed > fadeStart) {
            return 1.0 - (double) (elapsed - fadeStart) / BANNER_FADE_WINDOW_NS;
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
    public boolean isInvisible() {
        return masterRollPhase == RollPhase.INVISIBLE;
    }

    @Override
    public FadeMap getFadeMap() {
        return (masterRollPhase != RollPhase.NORMAL) ? fadeMap : null;
    }

    @Override
    public GameResult getResult() {
        RollPhase.GradeScale active = gradeTracker.getActiveGrade();
        long baseSortValue = 0L;

        if (active instanceof MasterGrade mg) {
            baseSortValue = mg.ordinal();
        } else if (active instanceof RollGrade rg) {
            baseSortValue = MasterGrade.values().length + rg.ordinal();
        }

        long sortValue = (baseSortValue * 1000L) + this.level;
        return new GameResult(gradeTracker.getDisplayGrade(), sortValue);
    }

    @Override
    public void debugSetLevel(int targetLevel) {
        level = Math.clamp(targetLevel, 0, MAX_LEVEL);
        sectionStartLevel = (level / 100) * 100;
        speedLevelStartLevel = level;
        lastSection = level / 100;
        checkSectionTransition(0.0, currentNow());
        ModeTables.applyMasterGravity(gravity, currentSpdLv, 0);
        syncHUDState(currentNow(), 0L);
    }

    public int getLevel()               { return level; }
    public int getCurrentSpeedLevel()   { return currentSpdLv; }
    public RollPhase getRollPhase()     { return masterRollPhase; }
    public GradeTracker getGradeTracker() { return gradeTracker; }
}