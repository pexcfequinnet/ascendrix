package org.example.ascendrix;

import java.util.HashMap;
import java.util.Map;

// Handles piece spawn delay, line clear delay
public class AREHandler {
    private long spawnDelayNs;
    private long lineClearDelayNs;
    private long areStartTime = -1;
    private boolean isLineClear = false;

    // Overrides per speed level
    private final Map<Integer, Long> spawnOverrides = new HashMap<>();
    private final Map<Integer, Long> lineClearOverrides = new HashMap<>();

    public void setOverride(int speedLevel, long spawnNs, long lineClearNs) {
        spawnOverrides.put(speedLevel, spawnNs);
        lineClearOverrides.put(speedLevel, lineClearNs);
    }

    public void clearOverrides() {
        spawnOverrides.clear();
        lineClearOverrides.clear();
    }
    public AREHandler(long spawnDelayNs, long lineClearDelayNs) {
        this.spawnDelayNs = spawnDelayNs;
        this.lineClearDelayNs = lineClearDelayNs;
    }

    public void trigger(boolean lineClear, long now) {
        this.isLineClear = lineClear;
        this.areStartTime = now;
    }

    public boolean isDone(long now) {
        if (areStartTime == -1) return true;
        long delay = isLineClear ? lineClearDelayNs : spawnDelayNs;
        return (now - areStartTime) >= delay;
    }

    public void reset() {
        areStartTime = -1;
        isLineClear = false;
    }

    public void setSpawnDelayNs(long spawnDelayNs) {
        this.spawnDelayNs = spawnDelayNs;
    }

    public void setLineClearDelayNs(long lineClearDelayNs) {
        this.lineClearDelayNs = lineClearDelayNs;
    }
}