package org.example.ascendrix.GameMode.Master;

import org.example.ascendrix.MainGame.Renderer.BoardRenderContext;
import org.example.ascendrix.Tetromino.TetrominoType;
import java.util.Arrays;

public class FadeMap implements BoardRenderContext.CellAlphaProvider {
    private static final long BOARD_FADE_INTERVAL_NS = 100_000_000L;
    private MasterRollPhase phase = MasterRollPhase.NORMAL;

    private final long[][] spawnTimes = new long[25][10];

    // Board fade fields
    private boolean fadingBoard = false;
    private int fadeRow = -1;
    private long lastFadeTime = -1;
    private long boardFadeCompleteTime = -1;

    private long currentFrameTime = -1;

    public void triggerBoardFade(int totalRows, MasterRollPhase currentPhase) {
        this.phase = currentPhase;

        fadingBoard = true;
        fadeRow = totalRows - 1;
        lastFadeTime = System.nanoTime();

        clearSpawnTimes();
    }

    public void updateBoardFade(long now, TetrominoType[][] board) {
        this.currentFrameTime = now;

        if (!fadingBoard) return;
        if (now - lastFadeTime < BOARD_FADE_INTERVAL_NS) return;

        for (int x = 0; x < board[0].length; x++) {
            board[fadeRow][x] = null;
            spawnTimes[fadeRow][x] = 0;
        }

        fadeRow--;
        lastFadeTime = now;

        if (fadeRow < 0) {
            fadingBoard = false;
            boardFadeCompleteTime = now; // roll starts here
        }
    }

    public boolean isFadingBoard() { return fadingBoard; }

    public void shiftRows(int clearedRow) {
        for (int y = clearedRow; y > 0; y--) {
            System.arraycopy(spawnTimes[y - 1], 0, spawnTimes[y], 0, 10);
        }
        Arrays.fill(spawnTimes[0], 0);
    }
    public void add(int[][] blocks, int x, int y) {
        long now = System.nanoTime();
        for (int[] p : blocks) {
            int bx = x + p[0];
            int by = y + p[1];
            if (bx >= 0 && bx < 10 && by >= 0 && by < 25) {
                spawnTimes[by][bx] = now;
            }
        }
    }

    @Override
    public double getAlpha(int x, int y) {
        if (boardFadeCompleteTime == -1) return 1.0;

        long spawnTime = spawnTimes[y][x];

        if (spawnTime == 0)
            return phase == MasterRollPhase.INVISIBLE ? 0.0 : 1.0;

        long delay    = phase == MasterRollPhase.INVISIBLE ? 0 : 3_000_000_000L;
        long duration = phase == MasterRollPhase.INVISIBLE ? 100_000_000L : 600_000_000L;

        long fadeTime = spawnTime + delay;

        long now = (currentFrameTime != -1) ? currentFrameTime : System.nanoTime();
        if (now < fadeTime) return 1.0;

        double t = (double)(now - fadeTime) / duration;
        return Math.max(0.0, 1.0 - t);
    }

    public void resetFadeStatus() {
        this.phase = MasterRollPhase.NORMAL;
        this.fadingBoard = false;
        this.boardFadeCompleteTime = -1;
        this.fadeRow = -1;
        clearSpawnTimes();
    }

    private void clearSpawnTimes() {
        for (long[] row : spawnTimes) {
            Arrays.fill(row, 0);
        }
    }

    public void copyRow(int fromY, int toY) {
        if (fromY >= 0 && fromY < 25 && toY >= 0 && toY < 25) {
            System.arraycopy(spawnTimes[fromY], 0, spawnTimes[toY], 0, 10);
        }
    }

    public void clearTopRows(int linesCleared) {
        for (int y = 0; y < linesCleared; y++) {
            if (y < 25) {
                java.util.Arrays.fill(spawnTimes[y], 0);
            }
        }
    }
}