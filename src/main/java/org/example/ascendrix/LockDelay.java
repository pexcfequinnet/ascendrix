package org.example.ascendrix;

public interface LockDelay {
    void update(long now, GameEngine game);
    void onMoveOrRotate(long now);
    void setLockResetLimit(int lockResetLimit);
    void setLockNs(long delayNs);
}
