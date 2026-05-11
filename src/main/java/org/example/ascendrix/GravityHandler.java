package org.example.ascendrix;
// Gravity config for Sprint
public class GravityHandler implements Gravity {

    private long lastFallTime = -1;
    private long fallNs;

    public GravityHandler(long fallNs) {
        this.fallNs = fallNs;
    }

    public void setFallNs(long fallNs) {
        this.fallNs = fallNs;
        this.lastFallTime = -1;
    }

    @Override
    public void update(long now, GameEngine game) {
        if (lastFallTime == -1) {
            lastFallTime = now;
            return;
        }

        if (now - lastFallTime < fallNs) return;

        // Multi-step handling
        long steps = (now - lastFallTime) / fallNs;

        for (int i = 0; i < steps; i++) {
            if (!game.tryFall()) {
                break;
            }
        }

        lastFallTime += steps * fallNs;
    }
}