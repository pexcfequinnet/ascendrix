package org.example.ascendrix.model.input;

import org.example.ascendrix.model.input.KeyBindingConfig.InputAction;
import org.example.ascendrix.model.mechanics.rotation.RotationDirection;

import java.util.ArrayDeque;

public class InputBuffer {

    private static final long NS_PER_FRAME = 1_000_000_000L / 60L;
    private static final long INPUT_POLL_NS = 1_000_000L; // 1000Hz (1ms)

    public static final int DEFAULT_DCD_FRAMES = 0;
    public static final int DEFAULT_IRS_BUFFER_FRAMES = 0;

    public record TimedInput(InputAction action, long timestampNs) {}

    // 1000Hz Discrete Action Queue
    private final ArrayDeque<TimedInput> actionQueue = new ArrayDeque<>();

    // IMS / DCD (Initial Movement System / DAS Cut Delay)
    private int bufferedDirection = 0;
    private long directionPressNs = -1L;
    private long imsBufferNs = DEFAULT_DCD_FRAMES * NS_PER_FRAME;
    private boolean imsConsumed = false;

    // IRS (Initial Rotation System)
    private long rotationPressNs = -1L;
    private long irsBufferNs = DEFAULT_IRS_BUFFER_FRAMES * NS_PER_FRAME;
    private RotationDirection bufferedRotation = RotationDirection.NONE;
    private boolean irsConsumed = false;

    // IHS (Initial Hold System)
    private boolean bufferedHold = false;

    /* ========================== 1000Hz ACTION QUEUE ========================== */

    public void enqueueAction(InputAction action, long nowNs) {
        if (action == null) return;
        long quantizedNs = (nowNs / INPUT_POLL_NS) * INPUT_POLL_NS;
        actionQueue.addLast(new TimedInput(action, quantizedNs));
    }

    public TimedInput pollActionUpTo(long simNowNs) {
        TimedInput next = actionQueue.peekFirst();
        if (next != null && next.timestampNs() <= simNowNs) {
            return actionQueue.pollFirst();
        }
        return null;
    }

    public boolean hasPendingActions() {
        return !actionQueue.isEmpty();
    }

    public void clearActionQueue() {
        actionQueue.clear();
    }

    /* ========================== IMS / DCD ========================== */

    public void setImsBufferFrames(int frames) {
        this.imsBufferNs = Math.max(0, frames) * NS_PER_FRAME;
    }

    public int getImsBufferFrames() {
        return (int) Math.round((double) imsBufferNs / NS_PER_FRAME);
    }

    public void bufferDirection(int dir) {
        bufferDirection(dir, System.nanoTime());
    }

    public void bufferDirection(int dir, long nowNs) {
        if (dir == 0) {
            clearDirection();
            return;
        }
        if (dir == bufferedDirection) return; // Ignore OS key-repeat events

        bufferedDirection = dir;
        directionPressNs = nowNs;
        imsConsumed = false;
    }

    /** Prevents a directional press that already moved an active piece from bleeding into IMS. */
    public void markDirectionConsumed() {
        imsConsumed = true;
    }

    public int getHeldIMS() {
        return getHeldIMS(System.nanoTime());
    }

    public int getHeldIMS(long nowNs) {
        if (bufferedDirection == 0 || imsConsumed || directionPressNs < 0L) {
            return 0;
        }
        if (nowNs - directionPressNs < imsBufferNs) {
            return 0;
        }
        imsConsumed = true;
        return bufferedDirection;
    }

    public int getBufferedDirection() {
        return bufferedDirection;
    }

    public void clearDirection() {
        bufferedDirection = 0;
        directionPressNs = -1L;
        imsConsumed = false;
    }

    /* ========================== IRS ========================== */

    public void bufferRotation(RotationDirection dir) {
        if (dir == null || dir == RotationDirection.NONE) {
            clearRotation();
            return;
        }
        if (dir == bufferedRotation) return; // Ignore OS key-repeat events

        bufferedRotation = dir;
        irsConsumed = false;
    }

    /** Prevents a rotation press that already rotated an active piece from bleeding into IRS. */
    public void markRotationConsumed() {
        irsConsumed = true;
    }

    public RotationDirection getHeldIRS() {
        if (bufferedRotation == RotationDirection.NONE || irsConsumed) {
            return null;
        }
        irsConsumed = true;
        return bufferedRotation;
    }

    public RotationDirection getHeldIRS(long nowNs) {
        return getHeldIRS();
    }

    public RotationDirection consumeRotation() {
        RotationDirection result = bufferedRotation;
        clearRotation();
        return result;
    }

    public void clearRotation() {
        bufferedRotation = RotationDirection.NONE;
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
        clearActionQueue();
        clearDirection();
        clearRotation();
        clearHold();
    }
}