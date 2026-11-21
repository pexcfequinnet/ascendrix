package org.example.ascendrix.model.input;

import org.example.ascendrix.model.mechanics.rotation.RotationDirection;

public class InputBuffer {
    private static final long NS_PER_FRAME = 1_000_000_000L / 60L;
    public static final int DEFAULT_IMS_BUFFER_FRAMES = 2; // ~50 ms at 60 FPS
    public static final int DEFAULT_IRS_BUFFER_FRAMES = 2; // ~50 ms at 60 FPS


    // IMS (Initial Movement System)
    private int bufferedDirection = 0;
    private long directionPressNs = -1L;
    private long imsBufferNs = DEFAULT_IMS_BUFFER_FRAMES * NS_PER_FRAME;
    private boolean imsConsumed = false;


    // IRS (Initial Rotation System)
    private RotationDirection bufferedRotation = RotationDirection.NONE;
    private long rotationPressNs = -1L;
    private long irsBufferNs = DEFAULT_IRS_BUFFER_FRAMES * NS_PER_FRAME;
    private boolean irsConsumed = false;

    // IHS (Initial Hold System)
    private boolean bufferedHold = false;

    /* ========================== IMS ========================== */

    public void setImsBufferFrames(int frames) {
        this.imsBufferNs = Math.max(0, frames) * NS_PER_FRAME;
    }

    public int getImsBufferFrames() {
        return (int) Math.round((double) imsBufferNs / NS_PER_FRAME);
    }

    public void bufferDirection(int dir) {
        bufferDirection(dir, System.nanoTime());
    }

    public void bufferDirection(int dir, long now) {
        if (dir == 0) {
            clearDirection();
            return;
        }
        // Ignore OS key-repeat events for the same direction so charge time isn't reset
        if (dir == bufferedDirection) {
            return;
        }
        bufferedDirection = dir;
        directionPressNs = now;
        imsConsumed = false;
    }

    /** Prevents a directional press that already moved an active piece from bleeding into IMS. */
    public void markDirectionConsumed() {
        imsConsumed = true;
    }

    public int getHeldIMS() {
        return getHeldIMS(System.nanoTime());
    }

    public int getHeldIMS(long now) {
        if (bufferedDirection == 0 || imsConsumed || directionPressNs < 0L) {
            return 0;
        }
        if (now - directionPressNs < imsBufferNs) {
            return 0;
        }
        imsConsumed = true;
        return bufferedDirection;
    }

    public void clearDirection() {
        bufferedDirection = 0;
        directionPressNs = -1L;
        imsConsumed = false;
    }

    public int getBufferedDirection() {
        return bufferedDirection;
    }

    /* ========================== IRS ========================== */

    public void setIrsBufferFrames(int frames) {
        this.irsBufferNs = Math.max(0, frames) * NS_PER_FRAME;
    }

    public int getIrsBufferFrames() {
        return (int) Math.round((double) irsBufferNs / NS_PER_FRAME);
    }

    public void bufferRotation(RotationDirection dir) {
        bufferRotation(dir, System.nanoTime());
    }

    public void bufferRotation(RotationDirection dir, long now) {
        if (dir == null || dir == RotationDirection.NONE) {
            clearRotation();
            return;
        }
        // Ignore OS key-repeat events for the same held direction so charge time isn't reset
        if (dir == bufferedRotation) {
            return;
        }
        bufferedRotation = dir;
        rotationPressNs = now;
        irsConsumed = false;
    }

    /** Prevents a rotation press that already rotated an active piece from bleeding into IRS. */
    public void markRotationConsumed() {
        irsConsumed = true;
    }

    public RotationDirection getHeldIRS() {
        return getHeldIRS(System.nanoTime());
    }

    public RotationDirection getHeldIRS(long now) {
        if (bufferedRotation == RotationDirection.NONE || irsConsumed || rotationPressNs < 0L) {
            return null;
        }
        if (now - rotationPressNs < irsBufferNs) {
            return null;
        }
        irsConsumed = true;
        return bufferedRotation;
    }

    public RotationDirection consumeRotation() {
        RotationDirection result = bufferedRotation;
        clearRotation();
        return result;
    }

    public void clearRotation() {
        bufferedRotation = RotationDirection.NONE;
        rotationPressNs = -1L;
        irsConsumed = false;
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