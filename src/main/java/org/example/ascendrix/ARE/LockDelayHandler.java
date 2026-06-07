package org.example.ascendrix.ARE;

import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.Tetromino.TetrominoHandler; // Import class này vào

public class LockDelayHandler implements LockDelay {
    private long lockNs;
    private long lockStartTime = -1;
    private long lastMoveTime = -1;
    private int lockResetCount = 0;
    private int lockResetLimit;

    private int lowestY = -1;

    private TetrominoHandler lastPiece = null;

    public void setLockResetLimit(int lockResetLimit) {
        this.lockResetLimit = lockResetLimit;
    }

    @Override
    public void setLockNs(long delayNs) {
        this.lockNs = delayNs;
    }

    public LockDelayHandler(long lockNs, int lockResetLimit) {
        this.lockNs = lockNs;
        this.lockResetLimit = lockResetLimit;
    }

    @Override
    public void update(long now, GameEngine game) {
        if (game.current == null) return;

        if (game.current != lastPiece) {
            reset();
            lastPiece = game.current;
        }

        int currentY = game.current.y;

        if (currentY > lowestY) {
            lowestY = currentY;
            resetLimitAndTimer(now);
        }

        if (game.isOnGround()) {
            if (lockStartTime == -1) {
                lockStartTime = now;
                lastMoveTime = now;
            }

            if (now - lastMoveTime >= lockNs) {
                reset();
                game.lockBlock(now);
            }
        } else {
            lockStartTime = -1;
            lastMoveTime = -1;
        }
    }

    @Override
    public void onMoveOrRotate(long now) {
        tryResetLockDelay(now);
    }

    private void tryResetLockDelay(long now) {
        if (lockStartTime != -1) {
            if (lockResetCount < lockResetLimit) {
                lockStartTime = now;
                lastMoveTime = now;
                lockResetCount++;
            }
        }
    }

    public void reset() {
        lockStartTime = -1;
        lastMoveTime = -1;
        lockResetCount = 0;
        lowestY = -1;
    }

    private void resetLimitAndTimer(long now) {
        lockStartTime = now;
        lastMoveTime = now;
        lockResetCount = 0;
    }
}