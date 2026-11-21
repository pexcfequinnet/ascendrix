package org.example.ascendrix.model.mechanics;

import org.example.ascendrix.model.state.GameSession;

import java.util.Objects;

public class MovementProcessor {

    private final HandlingConfig config;

    private long dasStartTime = -1L;
    private long lastArrTime = -1L;
    private int currentDir = 0;
    private long lastSdfTime = -1L;

    public MovementProcessor(HandlingConfig config) {
        this.config = Objects.requireNonNull(config, "HandlingConfig cannot be null");
    }

    public void update(long now, int horizontalDir, boolean softDropHeld, GameSession session) {
        if (session == null) return;
        handleHorizontal(now, horizontalDir, session);
        handleSDF(now, softDropHeld, session);
    }

    private void handleHorizontal(long now, int dir, GameSession session) {
        if (dir != currentDir) {
            currentDir = dir;
            if (dir == 0) {
                dasStartTime = -1L;
                lastArrTime = -1L;
            } else {
                dasStartTime = now;
                lastArrTime = now;
            }
            return;
        }

        if (dir == 0) {
            dasStartTime = -1L;
            lastArrTime = -1L;
            return;
        }

        // DAS still charging
        if (dasStartTime == -1L || now - dasStartTime < config.getDasNs()) {
            return;
        }

        // ARR phase
        if (config.isInstantArr() || config.getArrNs() <= 0L) {
            while (session.canMoveHorizontal(dir)) {
                session.move(dir);
            }
        } else if (lastArrTime == -1L || now - lastArrTime >= config.getArrNs()) {
            session.move(dir);
            lastArrTime = now;
        }
    }

    private void handleSDF(long now, boolean softDropHeld, GameSession session) {
        if (!softDropHeld) {
            lastSdfTime = -1L;
            return;
        }

        if (config.isInstantSdf() || config.getSdfNs() <= 0L) {
            while (!session.isOnGround()) {
                session.softDrop(now);
            }
            return;
        }

        if (lastSdfTime == -1L || now - lastSdfTime >= config.getSdfNs()) {
            session.softDrop(now);
            lastSdfTime = now;
        }
    }

    public boolean isDasCharged(long now) {
        return currentDir != 0
                && dasStartTime != -1L
                && now - dasStartTime >= config.getDasNs();
    }

    public int getDirection() {
        return currentDir;
    }

    public void resetDAS() {
        dasStartTime = -1L;
        lastArrTime = -1L;
    }

    public void reset() {
        currentDir = 0;
        dasStartTime = -1L;
        lastArrTime = -1L;
        lastSdfTime = -1L;
    }

    public HandlingConfig getConfig() {
        return config;
    }
}