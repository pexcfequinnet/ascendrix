package org.example.ascendrix.model.input;

import org.example.ascendrix.model.mechanics.rotation.RotationDirection;

public class InputBuffer {

    // IMS (Initial Movement System)
    private int bufferedDirection = 0;

    // IRS (Initial Rotation System)
    private RotationDirection bufferedRotation = RotationDirection.NONE;

    // IHS (Initial Hold System)
    private boolean bufferedHold = false;

    /* ========================== IMS ========================== */

    public void bufferDirection(int dir) {
        bufferedDirection = dir;
    }

    public void clearDirection() {
        bufferedDirection = 0;
    }

    public int getBufferedDirection() {
        return bufferedDirection;
    }

    /* ========================== IRS ========================== */

    public void bufferRotation(RotationDirection dir) {
        bufferedRotation = (dir != null) ? dir : RotationDirection.NONE;
    }

    public RotationDirection getHeldIRS() {
        return (bufferedRotation != RotationDirection.NONE) ? bufferedRotation : null;
    }

    public RotationDirection consumeRotation() {
        RotationDirection result = bufferedRotation;
        bufferedRotation = RotationDirection.NONE;
        return result;
    }

    public void clearRotation() {
        bufferedRotation = RotationDirection.NONE;
    }

    /* ========================== IHS ========================== */

    public void bufferHold() {
        bufferedHold = true;
    }

    public void clearHold() {
        bufferedHold = false;
    }

    public boolean isHoldHeld() {
        return bufferedHold;
    }

    /* ========================== RESET ========================== */

    public void clearAll() {
        clearDirection();
        clearRotation();
        clearHold();
    }
}