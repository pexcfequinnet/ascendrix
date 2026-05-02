package org.example.ascendrix;
// Gravity config for Sprint
public class StandardGRaLockD implements Gravity {

    private long lastFallTime = -1;
    private long fallNs;

    public StandardGRaLockD(long fallNs) {
        this.fallNs = fallNs;
    }

    public void setFallNs(long fallNs) {
        this.fallNs = fallNs;
    }

    @Override
    public void update(long now, GameEngine game) {
        System.out.println("Updating gravity instance: " + System.identityHashCode(this) + " fallNs: " + fallNs);
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