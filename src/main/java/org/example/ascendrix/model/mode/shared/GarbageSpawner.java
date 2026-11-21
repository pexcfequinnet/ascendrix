package org.example.ascendrix.model.mode.shared;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.state.GameSession;

public class GarbageSpawner {

    private long lastGarbageTime = -1L;

    public void update(long now, int level, GameSession session) {
        long interval = ModeTables.getOverdriveGarbageInterval(level);
        if (interval == ModeTables.OVERDRIVE_GARBAGE_PAUSED) {
            lastGarbageTime = -1L; // Reset timer while garbage is paused
            return;
        }

        if (lastGarbageTime == -1L) {
            lastGarbageTime = now;
            return;
        }

        if (now - lastGarbageTime >= interval) {
            spawnGarbage(session);
            lastGarbageTime = now;
        }
    }

    private void spawnGarbage(GameSession session) {
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
    }
}