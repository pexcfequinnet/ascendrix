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

public class SprintRuleset extends BaseModeRuleset {

    public static final int DEFAULT_TARGET_LINES = 40;
    private static final double SPRINT_GRAVITY_G = 1.0 / 60.0; // 1G / 60fps (~1 cell/sec)
    private static final long LOCK_DELAY_NS = 500_000_000L;     // 500 ms
    private static final int LOCK_RESET_LIMIT = 15;

    private final int targetLines;
    private int linesCleared = 0;
    private boolean b2bActive = false;
    private int b2bStreak = 0;
    private long finalTimeMs = 0L;
    private long lastNowNs = 0L;
    private int piecesPlaced = 0;
    private SprintRuleset(int targetLines,
                          MovementProcessor handling,
                          GravityModel gravity,
                          LockDelay lockDelay,
                          ARETimer are,
                          RotationSystem rotationSystem) {
        super(GameMode.SPRINT, handling, gravity, lockDelay, are, rotationSystem);
        this.targetLines = targetLines;
        this.hudState.updateStats(0, targetLines, "", 0L);
    }

    public static SprintRuleset create() {
        return create(DEFAULT_TARGET_LINES, null);
    }

    public static SprintRuleset create(HandlingConfig playerHandling) {
        return create(DEFAULT_TARGET_LINES, playerHandling);
    }

    public static SprintRuleset create(int targetLines, HandlingConfig playerHandling) {
        HandlingConfig config = (playerHandling != null)
                ? playerHandling.copy()
                : HandlingConfig.defaultConfig();

        return new SprintRuleset(
                targetLines,
                new MovementProcessor(config),
                new GravityModel(SPRINT_GRAVITY_G),
                new LockDelayTimer(LOCK_DELAY_NS, LOCK_RESET_LIMIT),
                new ARETimer(0L, 0L, 0L),
                new SRS()
        );
    }

    /* ========================== LIFECYCLE HOOKS ========================== */

    @Override
    public void update(long now, GameSession session) {
        this.lastNowNs = now;
        long elapsedMs = session.getTimer().getElapsedMs(now);
        hudState.updateStats(linesCleared, targetLines, "", elapsedMs);
        hudState.setB2bStreak(b2bActive ? b2bStreak : 0);
    }

    @Override
    public void onLinesCleared(int lines,
                               SpinType spin,
                               int dropRows,
                               TetrominoQueue.DropType dropType,
                               GameSession session) {
        long eventNow = (lastNowNs > 0L) ? lastNowNs : System.nanoTime();

        if (lines > 0) {
            linesCleared += lines;
            boolean isB2B = (lines == 4) || (spin != null && spin != SpinType.NONE);

            if (isB2B && b2bActive) {
                b2bStreak++;
            } else {
                b2bStreak = 0;
            }
            b2bActive = isB2B;
        }

        long elapsedMs = session.getTimer().getElapsedMs(eventNow);
        hudState.updateStats(linesCleared, targetLines, "", elapsedMs);
        hudState.setB2bStreak(b2bActive ? b2bStreak : 0);
        hudState.recordClear(spin, lines, eventNow);

        if (linesCleared >= targetLines) {
            this.finalTimeMs = elapsedMs;
            session.clearGame();
        }
    }

    @Override
    public void onPiecePlaced(GameSession session) {
        piecesPlaced++;
    }

    public int getPiecesPlaced() {
        return piecesPlaced;
    }

    /* ========================== CAPABILITIES & RESULT ========================== */

    @Override
    public boolean supportsIRS() {
        return false;
    }

    @Override
    public boolean supportsPerfectClear() {
        return true;
    }

    @Override
    public GameResult getResult() {
        return new GameResult("", finalTimeMs, finalTimeMs);
    }
    public int getLinesCleared() { return linesCleared; }
    public int getTargetLines()  { return targetLines; }
    public int getB2bStreak()    { return b2bActive ? b2bStreak : 0; }
}