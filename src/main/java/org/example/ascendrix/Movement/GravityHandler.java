package org.example.ascendrix.Movement;

import org.example.ascendrix.MainGame.Engine.GameEngine;

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

        long steps = (now - lastFallTime) / fallNs;
        steps = Math.min(steps, 25); // cap at 20 steps (1 full board height)

        for (int i = 0; i < steps; i++) {
            if (!game.tryFall()) break;
        }

        lastFallTime += steps * fallNs;
    }
}