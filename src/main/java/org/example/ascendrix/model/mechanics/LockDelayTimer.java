package org.example.ascendrix.model.mechanics;

import org.example.ascendrix.model.piece.ActivePiece;
import org.example.ascendrix.model.state.GameSession;

public class LockDelayTimer implements LockDelay {
    private long lockNs;
    private long lockStartTime = -1;
    private long lastMoveTime = -1;
    private int lockResetCount = 0;
    private int lockResetLimit;

    private int lowestY = -1;
    private ActivePiece lastPiece = null;

    public LockDelayTimer(long lockNs, int lockResetLimit) {
        this.lockNs = lockNs;
        this.lockResetLimit = lockResetLimit;
    }

    @Override
    public void update(long now, GameSession session) {
        ActivePiece current = session.getCurrentPiece();
        if (current == null) return;

        if (current != lastPiece) {
            reset();
            lastPiece = current;
        }

        int currentY = current.getY();
        if (currentY > lowestY) {
            lowestY = currentY;
            resetLimitAndTimer(now);
        }

        if (session.isOnGround()) {
            if (lockStartTime == -1) {
                lockStartTime = now;
                lastMoveTime = now;
            }

            if (now - lastMoveTime >= lockNs) {
                reset();
                session.lockBlock(now);
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
        if (lockStartTime != -1 && lockResetCount < lockResetLimit) {
            lockStartTime = now;
            lastMoveTime = now;
            lockResetCount++;
        }
    }

    public void reset() {
        lockStartTime = -1;
        lastMoveTime = -1;
        lockResetCount = 0;
        lowestY = -1;
        lastPiece = null;
    }

    private void resetLimitAndTimer(long now) {
        lockStartTime = now;
        lastMoveTime = now;
        lockResetCount = 0;
    }

    /* ========================== GETTERS & SETTERS ========================== */

    @Override
    public void setLockResetLimit(int lockResetLimit) {
        this.lockResetLimit = lockResetLimit;
    }

    @Override
    public void setLockNs(long delayNs) {
        this.lockNs = delayNs;
    }

    public long getLockNs()            { return lockNs; }
    public int getLockResetCount()     { return lockResetCount; }
    public int getLockResetLimit()     { return lockResetLimit; }

    public double getLockProgress(long now) {
        if (lockStartTime == -1 || lockNs <= 0) return 0.0;
        return Math.min(1.0, Math.max(0.0, (double) (now - lastMoveTime) / lockNs));
    }
}