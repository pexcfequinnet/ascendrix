package org.example.ascendrix;
// Gravity config for Sprint
public class SprintGravity implements Gravity {

    private long lastFallTime = -1;
    private final long fallNs;

    public SprintGravity(long fallNs) {
        this.fallNs = fallNs;
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