package org.example.ascendrix.ARE;

import org.example.ascendrix.MainGame.Engine.GameEngine;

public class LockDelayHandler implements LockDelay {
    private long lockNs;
    private long lockStartTime = -1;
    private long lastMoveTime = -1;  // separate from lockStartTime
    private int lockResetCount = 0;
    private int lockResetLimit;

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
        if (game.isOnGround()) {
            if (lockStartTime == -1) {
                lockStartTime = now;
                lastMoveTime = now;
            }

            // Check against last move time, not lock start
            if (now - lastMoveTime >= lockNs) {
                reset();
                game.lockBlock(now);
            }
        } else {
            reset();
        }
    }

    @Override
    public void onMoveOrRotate(long now) {
        tryResetLockDelay(now);
    }



    private void tryResetLockDelay(long now) {
        if (lockStartTime != -1 && lockResetCount < lockResetLimit) {
            lockStartTime = now; // reset the timer itself, not just lastMoveTime
            lastMoveTime = now;
            lockResetCount++;
        }
    }

    private void reset() {
        lockStartTime = -1;
        lastMoveTime = -1;
        lockResetCount = 0;
    }
}