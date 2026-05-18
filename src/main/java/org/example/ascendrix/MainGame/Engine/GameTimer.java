package org.example.ascendrix.MainGame.Engine;

public class GameTimer {
    private long startTime = 0;
    private long accumulated = 0;
    private boolean running = false;

    public void start() {
        if (!running) {
            startTime = System.currentTimeMillis();
            running = true;
        }
    }

    public void pause() {
        if (running) {
            accumulated += System.currentTimeMillis() - startTime;
            running = false;
        }
    }

    public void reset() {
        startTime = -1;
        accumulated = 0;
        running = false;
    }
    // For marathon + sprint
    public long getElapsedMs() {
        if (running) {
            return accumulated + (System.currentTimeMillis() - startTime);
        }
        return accumulated;
    }
    // For master + overdrive mode
    public double getElapsedSeconds() {return getElapsedMs() / 1000.0;}

    public boolean isRunning() {
        return running;
    }

    public static String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long centiseconds = (ms % 1000) / 10;

        return String.format("%02d:%02d.%02d", minutes, seconds, centiseconds);
    }
}