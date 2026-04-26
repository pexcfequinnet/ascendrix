package org.example.ascendrix;

public interface LockDelay {
    void update(long frame, GameEngine game);
    void onMoveOrRotate(long frame);
}
