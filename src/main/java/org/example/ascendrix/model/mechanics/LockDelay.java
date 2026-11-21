package org.example.ascendrix.model.mechanics;

import org.example.ascendrix.model.state.GameSession;

public interface LockDelay {
    void update(long now, GameSession session);
    void onMoveOrRotate(long now);
    void setLockResetLimit(int lockResetLimit);
    void setLockNs(long delayNs);
}