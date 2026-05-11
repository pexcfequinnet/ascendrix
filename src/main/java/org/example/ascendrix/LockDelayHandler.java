package org.example.ascendrix;

public class LockDelayHandler implements LockDelay {
    private long lockNs;

    private long lockStartTime = -1;
    private long lastResetTime = -1;
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

            // Start counting whe block is on ground
            if (lockStartTime == -1) {
                lockStartTime = now;
                lastResetTime = now;
            }

            // Lock delay check
            if (now - lastResetTime >= lockNs) {
                reset();
                game.lockBlock();
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
            lastResetTime = now;
            lockResetCount++;
        }
    }

    private void reset() {
        lockStartTime = -1;
        lastResetTime = -1;
        lockResetCount = 0;
    }


}