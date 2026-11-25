package org.example.ascendrix.model.board;

import org.example.ascendrix.model.mode.shared.RollPhase;
import org.example.ascendrix.model.piece.TetrominoType;

import java.util.Arrays;

public class FadeMap {

    private static final long BOARD_FADE_INTERVAL_NS  = 100_000_000L;   // 100 ms per row
    private static final long FADING_DELAY_NS         = 3_000_000_000L; // 3.0 s default visible before fade
    private static final long FADING_DURATION_NS      = 600_000_000L;   // 600 ms default fade-out
    private static final long INVISIBLE_DURATION_NS   = 100_000_000L;   // 100 ms flash on lock

    private RollPhase phase = RollPhase.NORMAL;
    private final long[][] spawnTimes = new long[Board.ROWS][Board.COLS];
    private long customFadingDelayNs = FADING_DELAY_NS;
    private long customFadingDurationNs = FADING_DURATION_NS;

    // Board wipe-fade state
    private boolean fadingBoard = false;
    private int fadeRow = -1;
    private long lastFadeTime = -1L;
    private long boardFadeCompleteTime = -1L;
    private long currentFrameTime = -1L;

    /* ========================== NON-WIPING FADING ========================== */

    public void activateFading(long now, long fadingDelayNs) {
        activateFading(now, fadingDelayNs, FADING_DURATION_NS);
    }

    public void activateFading(long now, long fadingDelayNs, long fadingDurationNs) {
        this.phase = RollPhase.FADING;
        this.fadingBoard = false;
        this.boardFadeCompleteTime = now;
        this.currentFrameTime = now;
        setFadingTimingNs(fadingDelayNs, fadingDurationNs);
    }

    public void setFadingDelayNs(long fadingDelayNs) {
        this.customFadingDelayNs = Math.max(0L, fadingDelayNs);
    }

    public void setFadingTimingNs(long fadingDelayNs, long fadingDurationNs) {
        this.customFadingDelayNs = Math.max(0L, fadingDelayNs);
        this.customFadingDurationNs = Math.max(1L, fadingDurationNs);
    }

    /* ========================== BOARD WIPE FADE ========================== */

    public void triggerBoardFade(int totalRows, RollPhase currentPhase) {
        triggerBoardFade(totalRows, currentPhase,
                (currentFrameTime != -1L) ? currentFrameTime : System.nanoTime());
    }

    public void triggerBoardFade(int totalRows, RollPhase currentPhase, long now) {
        this.phase = (currentPhase != null) ? currentPhase : RollPhase.NORMAL;
        this.fadingBoard = true;
        this.fadeRow = totalRows - 1;
        this.lastFadeTime = now;
        this.customFadingDelayNs = FADING_DELAY_NS;
        this.customFadingDurationNs = FADING_DURATION_NS;
        clearSpawnTimes();
    }

    public void updateBoardFade(long now, TetrominoType[][] board) {
        this.currentFrameTime = now;

        if (!fadingBoard) return;
        if (now - lastFadeTime < BOARD_FADE_INTERVAL_NS) return;

        if (fadeRow >= 0 && fadeRow < board.length) {
            Arrays.fill(board[fadeRow], null);
            Arrays.fill(spawnTimes[fadeRow], 0L);
        }

        fadeRow--;
        lastFadeTime = now;

        if (fadeRow < 0) {
            fadingBoard = false;
            boardFadeCompleteTime = now; // Roll phase visibility rules start here
        }
    }

    public boolean isFadingBoard() {
        return fadingBoard;
    }

    /* ========================== PIECE LOCK & ALPHA ========================== */

    public void add(int[][] blocks, int x, int y) {
        long now = (currentFrameTime != -1L) ? currentFrameTime : System.nanoTime();
        add(blocks, x, y, now);
    }

    public void add(int[][] blocks, int x, int y, long now) {
        for (int[] p : blocks) {
            int bx = x + p[0];
            int by = y + p[1];
            if (bx >= 0 && bx < Board.COLS && by >= 0 && by < Board.ROWS) {
                spawnTimes[by][bx] = now;
            }
        }
    }

    public double getAlpha(int x, int y) {
        if (boardFadeCompleteTime == -1L) return 1.0;
        if (y < 0 || y >= Board.ROWS || x < 0 || x >= Board.COLS) return 1.0;

        long spawnTime = spawnTimes[y][x];
        if (spawnTime == 0L) {
            return (phase == RollPhase.INVISIBLE) ? 0.0 : 1.0;
        }

        long delay    = (phase == RollPhase.INVISIBLE) ? 0L : customFadingDelayNs;
        long duration = (phase == RollPhase.INVISIBLE) ? INVISIBLE_DURATION_NS : customFadingDurationNs;

        long fadeTime = spawnTime + delay;
        long now = (currentFrameTime != -1L) ? currentFrameTime : System.nanoTime();
        if (now < fadeTime) return 1.0;

        double t = (double) (now - fadeTime) / duration;
        return Math.max(0.0, 1.0 - t);
    }

    /* ========================== ROW OPERATIONS & RESET ========================== */

    public void shiftRows(int clearedRow) {
        for (int y = clearedRow; y > 0; y--) {
            System.arraycopy(spawnTimes[y - 1], 0, spawnTimes[y], 0, Board.COLS);
        }
        Arrays.fill(spawnTimes[0], 0L);
    }

    public void shiftRowsUp() {
        for (int y = 0; y < Board.ROWS - 1; y++) {
            System.arraycopy(spawnTimes[y + 1], 0, spawnTimes[y], 0, Board.COLS);
        }
        Arrays.fill(spawnTimes[Board.ROWS - 1], 0L);
    }

    public void copyRow(int fromY, int toY) {
        if (fromY >= 0 && fromY < Board.ROWS && toY >= 0 && toY < Board.ROWS) {
            System.arraycopy(spawnTimes[fromY], 0, spawnTimes[toY], 0, Board.COLS);
        }
    }

    public void clearTopRows(int linesCleared) {
        int limit = Math.min(linesCleared, Board.ROWS);
        for (int y = 0; y < limit; y++) {
            Arrays.fill(spawnTimes[y], 0L);
        }
    }

    public void resetFadeStatus() {
        this.phase = RollPhase.NORMAL;
        this.fadingBoard = false;
        this.boardFadeCompleteTime = -1L;
        this.fadeRow = -1;
        this.lastFadeTime = -1L;
        this.currentFrameTime = -1L;
        this.customFadingDelayNs = FADING_DELAY_NS;
        this.customFadingDurationNs = FADING_DURATION_NS;
        clearSpawnTimes();
    }

    private void clearSpawnTimes() {
        for (long[] row : spawnTimes) {
            Arrays.fill(row, 0L);
        }
    }

    public RollPhase getPhase() {
        return phase;
    }
}