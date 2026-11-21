package org.example.ascendrix.model.state;

public class GameTimer {
    private long startTimeNs = 0L;
    private long accumulatedNs = 0L;
    private boolean running = false;

    /* ========================== LIFECYCLE ========================== */

    public void start() {
        start(System.nanoTime());
    }

    public void start(long nowNs) {
        if (!running) {
            startTimeNs = nowNs;
            running = true;
        }
    }

    public void pause() {
        pause(System.nanoTime());
    }

    public void pause(long nowNs) {
        if (running) {
            accumulatedNs += Math.max(0L, nowNs - startTimeNs);
            running = false;
        }
    }

    public void resume() {
        start(System.nanoTime());
    }

    public void resume(long nowNs) {
        start(nowNs);
    }

    public void reset() {
        startTimeNs = 0L;
        accumulatedNs = 0L;
        running = false;
    }

    /* ========================== QUERIES ========================== */

    public long getElapsedNs() {
        return getElapsedNs(System.nanoTime());
    }

    public long getElapsedNs(long nowNs) {
        if (running) {
            return accumulatedNs + Math.max(0L, nowNs - startTimeNs);
        }
        return accumulatedNs;
    }

    // For Marathon + Sprint + Ultra
    public long getElapsedMs() {
        return getElapsedNs() / 1_000_000L;
    }

    public long getElapsedMs(long nowNs) {
        return getElapsedNs(nowNs) / 1_000_000L;
    }

    // For Master + Overdrive checkpoints & grading
    public double getElapsedSeconds() {
        return getElapsedNs() / 1_000_000_000.0;
    }

    public double getElapsedSeconds(long nowNs) {
        return getElapsedNs(nowNs) / 1_000_000_000.0;
    }

    public boolean isRunning() {
        return running;
    }
}
