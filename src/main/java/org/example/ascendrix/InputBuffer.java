package org.example.ascendrix;

public class InputBuffer {

    // IRS
    private boolean bufferedRotateCW;
    private boolean bufferedRotateCCW;

    // IHS
    private boolean bufferedHold;

    public void bufferRotateCW() {
        bufferedRotateCW = true;
    }

    public void bufferRotateCCW() {
        bufferedRotateCCW = true;
    }

    public void bufferHold() {
        bufferedHold = true;
    }

    public void clear() {
        bufferedRotateCW = false;
        bufferedRotateCCW = false;
        bufferedHold = false;
    }

    public boolean consumeRotateCW() {
        boolean v = bufferedRotateCW;
        bufferedRotateCW = false;
        return v;
    }

    public boolean consumeRotateCCW() {
        boolean v = bufferedRotateCCW;
        bufferedRotateCCW = false;
        return v;
    }

    public boolean consumeHold() {
        boolean v = bufferedHold;
        bufferedHold = false;
        return v;
    }
}