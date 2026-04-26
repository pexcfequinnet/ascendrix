package org.example.ascendrix;

public class SprintLockDelay implements LockDelay {
    private final int lockFrames;
    private long groundedSinceFrame = -1;

    public SprintLockDelay(int lockFrames) {
        this.lockFrames = lockFrames;
    }

    @Override
    public void update(long frame, GameEngine game) {
        if (game.isOnGround()) {
            if (groundedSinceFrame == -1) groundedSinceFrame = frame;

            if (frame - groundedSinceFrame >= lockFrames) {
                groundedSinceFrame = -1;
                game.lock();
            }
        } else {
            groundedSinceFrame = -1;
        }
    }

    @Override
    public void onMoveOrRotate(long frame) {
        groundedSinceFrame = frame; // reset the lock timer
    }
}