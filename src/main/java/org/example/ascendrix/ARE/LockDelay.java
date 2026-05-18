package org.example.ascendrix.ARE;

import org.example.ascendrix.MainGame.Engine.GameEngine;

public interface LockDelay {
    void update(long now, GameEngine game);
    void onMoveOrRotate(long now);
    void setLockResetLimit(int lockResetLimit);
    void setLockNs(long delayNs);
}
