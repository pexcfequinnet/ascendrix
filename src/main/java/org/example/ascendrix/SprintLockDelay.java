package org.example.ascendrix;

public class SprintLockDelay implements LockDelay {
    private final long lockNs;

    private long lockStartTime = -1;
    private long lastResetTime = -1;


    public SprintLockDelay(long lockNs) {
        this.lockNs = lockNs;
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
                lockStartTime = -1;
                lastResetTime = -1;
                game.lock();
            }

        } else {
            lockStartTime = -1;
            lastResetTime = -1;
        }
    }

    @Override
    public void onMoveOrRotate(long now) {
        if (lockStartTime != -1) {
            lastResetTime = now;
        }
    }
}