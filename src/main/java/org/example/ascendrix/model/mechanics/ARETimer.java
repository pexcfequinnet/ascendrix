package org.example.ascendrix.model.mechanics;

import java.util.HashMap;
import java.util.Map;

// Handles piece entry delay (ARE) and line-clear delay
public class ARETimer {
    private final long baseSpawnDelayNs;
    private final long baseLineClearDelayNs;

    private long spawnDelayNs;
    private long lineClearDelayNs;
    private long clearAnimNs;

    private long areStartTime = -1;
    private boolean isLineClear = false;

    // Overrides per speed level
    private final Map<Integer, Long> spawnOverrides = new HashMap<>();
    private final Map<Integer, Long> lineClearOverrides = new HashMap<>();

    public ARETimer(long spawnDelayNs, long lineClearDelayNs, long clearAnimNs) {
        this.baseSpawnDelayNs = spawnDelayNs;
        this.baseLineClearDelayNs = lineClearDelayNs;
        this.spawnDelayNs = spawnDelayNs;
        this.lineClearDelayNs = lineClearDelayNs;
        this.clearAnimNs = clearAnimNs;
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

    public boolean isActive() {
        return areStartTime != -1;
    }

    public void reset() {
        areStartTime = -1;
        isLineClear = false;
    }

    /* ========================== SPEED OVERRIDES ========================== */

    public void setOverride(int speedLevel, long spawnNs, long lineClearNs) {
        spawnOverrides.put(speedLevel, spawnNs);
        lineClearOverrides.put(speedLevel, lineClearNs);
    }

    public void applySpeedLevel(int speedLevel) {
        this.spawnDelayNs = spawnOverrides.getOrDefault(speedLevel, baseSpawnDelayNs);
        this.lineClearDelayNs = lineClearOverrides.getOrDefault(speedLevel, baseLineClearDelayNs);
    }

    public void clearOverrides() {
        spawnOverrides.clear();
        lineClearOverrides.clear();
        this.spawnDelayNs = baseSpawnDelayNs;
        this.lineClearDelayNs = baseLineClearDelayNs;
    }

    /* ========================== GETTERS & SETTERS ========================== */

    public void setSpawnDelayNs(long spawnDelayNs) {
        this.spawnDelayNs = spawnDelayNs;
    }

    public void setLineClearDelayNs(long lineClearDelayNs) {
        this.lineClearDelayNs = lineClearDelayNs;
    }

    public void setClearAnimNs(long clearAnimNs) {
        this.clearAnimNs = clearAnimNs;
    }

    public long getSpawnDelayNs()     { return spawnDelayNs; }
    public long getLineClearDelayNs() { return lineClearDelayNs; }
    public long getClearAnimNs()      { return clearAnimNs; }
}