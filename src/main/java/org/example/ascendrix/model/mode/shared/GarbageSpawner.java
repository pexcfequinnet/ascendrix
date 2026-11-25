package org.example.ascendrix.model.mode.shared;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.state.GameSession;

public class GarbageSpawner {

    public static final long PAUSED = -1L;

    private long lastGarbageTime = -1L;
    private int piecesSinceGarbage = 0;

    /**
     * Time-driven garbage spawning (e.g., Overdrive).
     * Pass {@link #PAUSED} (or any value {@code <= 0}) to pause and reset the timer.
     */
    public void update(long now, long intervalNs, GameSession session) {
        if (intervalNs <= 0L) {
            lastGarbageTime = -1L;
            return;
        }

        if (lastGarbageTime == -1L) {
            lastGarbageTime = now;
            return;
        }

        if (now - lastGarbageTime >= intervalNs) {
            spawnGarbage(session);
            lastGarbageTime = now;
        }
    }

    /**
     * Piece-count-driven garbage spawning (e.g., Master 1.1 / Shirase-style modes).
     * Pass {@code <= 0} to pause and reset the piece counter.
     */
    public void onPiecePlaced(int pieceInterval, GameSession session) {
        if (pieceInterval <= 0) {
            piecesSinceGarbage = 0;
            return;
        }

        piecesSinceGarbage++;
        if (piecesSinceGarbage >= pieceInterval) {
            spawnGarbage(session);
            piecesSinceGarbage = 0;
        }
    }

    /**
     * Pushes the stack up by one row and fills the bottom row with garbage,
     * preserving empty columns from the previous bottom row as gaps.
     */
    public void spawnGarbage(GameSession session) {
        Board board = session.getBoard();
        int bottomRow = Board.ROWS - 1;

        // Record empty columns on the bottom row before pushing — these become garbage gaps
        boolean[] isEmpty = new boolean[Board.COLS];
        for (int col = 0; col < Board.COLS; col++) {
            isEmpty[col] = board.isCellEmpty(bottomRow, col);
        }

        // Push board and active piece up by one row (triggers top-out if row 0 is occupied)
        if (!session.pushBoardUp()) {
            return;
        }

        // Fill bottom row with garbage except inherited gaps
        for (int col = 0; col < Board.COLS; col++) {
            if (!isEmpty[col]) {
                board.setGarbageCell(bottomRow, col);
            }
        }
    }

    public void reset() {
        lastGarbageTime = -1L;
        piecesSinceGarbage = 0;
    }
}