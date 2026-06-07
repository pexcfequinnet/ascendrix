package org.example.ascendrix.Input;

import org.example.ascendrix.Rotation.RotationDirection;

public class InputBuffer {
    // IMS
    private int bufferedDirection = 0;
    public void bufferDirection(int dir) {
        bufferedDirection = dir;
    }
    public void clearDirection() {
        bufferedDirection = 0;
    }
    public int getBufferedDirection() {
        return bufferedDirection;
    }
    // IRS
    private RotationBuffer rotationBuffer = RotationBuffer.NONE;
    private RotationDirection bufferedRotation = RotationDirection.NONE;

    public RotationDirection consumeRotation() {
        RotationDirection result = bufferedRotation;
        bufferedRotation = null;
        return result;
    }

    // IHS
    private boolean bufferedHold;

    public void bufferHold() {
        bufferedHold = true;
    }

    public void clearHold()      { bufferedHold      = false; }
    public boolean isHoldHeld()      { return bufferedHold; }

    public void bufferRotation(RotationDirection dir) {
        bufferedRotation = dir;
    }
}