package org.example.ascendrix.GameMode.Overdrive;

import org.example.ascendrix.MainGame.Engine.GameEngine;

import java.util.ArrayList;
import java.util.List;

public class OverdriveGarbageHandler {
    private long lastGarbageTime = -1;

    public void update(long now, int level, GameEngine game) {
        long interval = OverdriveGarbageTable.getInterval(level);
        if (interval == OverdriveGarbageTable.PAUSED) {
            lastGarbageTime = -1; // reset timer when paused
            return;
        }

        if (lastGarbageTime == -1) {
            lastGarbageTime = now;
            return;
        }

        if (now - lastGarbageTime >= interval) {
            spawnGarbage(game);
            lastGarbageTime = now;
        }
    }

    private void spawnGarbage(GameEngine game) {
        int cols = game.getCols();
        int rows = game.getRows();

        // find empty columns before pushing — these become gaps
        boolean[] isEmpty = new boolean[cols];
        for (int col = 0; col < cols; col++) {
            isEmpty[col] = game.isColumnEmpty(col);
        }

        // push entire board up by one row
        if (!game.pushBoardUp()) return; // tops out

        // fill bottom row with garbage except gaps
        for (int col = 0; col < cols; col++) {
            if (!isEmpty[col]) {
                game.setGarbageCell(rows - 1, col);
            }
        }
    }

    public void reset() {
        lastGarbageTime = -1;
    }
}