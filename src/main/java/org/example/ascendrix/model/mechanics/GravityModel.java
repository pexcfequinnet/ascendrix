package org.example.ascendrix.model.mechanics;

import org.example.ascendrix.model.state.GameSession;

public class GravityModel {

    private static final double FRAME_NS = 1_000_000_000.0 / 60.0;
    private static final int MAX_STEPS_PER_TICK = 25; // Covers full board height + hidden buffer

    private long lastFallTime = -1L;
    private long fallNs;
    private double g;

    public GravityModel(double g) {
        setG(g);
    }

    public static GravityModel fromFallNs(long fallNs) {
        GravityModel model = new GravityModel(1.0 / 60.0);
        model.setFallNs(fallNs);
        return model;
    }

    public void setG(double g) {
        double safeG = Math.max(g, 1e-6);
        long computedFallNs = Math.max(1L, Math.round(FRAME_NS / safeG));
        this.g = safeG;
        if (this.fallNs != computedFallNs) {
            this.fallNs = computedFallNs;
            this.lastFallTime = -1L;
        }
    }

    public void setFallNs(long fallNs) {
        long clampedFallNs = Math.max(1L, fallNs);
        if (this.fallNs == clampedFallNs) {
            return;
        }
        this.fallNs = clampedFallNs;
        this.g = FRAME_NS / clampedFallNs;
        this.lastFallTime = -1L;
    }

    public void reset() {
        this.lastFallTime = -1L;
    }

    public void update(long now, GameSession session) {
        if (fallNs <= 0L) return;

        if (lastFallTime == -1L) {
            lastFallTime = now;
            return;
        }

        long elapsed = now - lastFallTime;
        if (elapsed < fallNs) return;

        long steps = Math.min(elapsed / fallNs, MAX_STEPS_PER_TICK);
        boolean grounded = false;

        for (int i = 0; i < steps; i++) {
            if (!session.tryFall()) {
                grounded = true;
                break;
            }
        }

        if (grounded) {
            lastFallTime = now;
        } else {
            lastFallTime += steps * fallNs;
        }
    }

    public long getFallNs() {
        return fallNs;
    }

    public double getG() {
        return g;
    }
}