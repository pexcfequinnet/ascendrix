package org.example.ascendrix.Movement;

import org.example.ascendrix.Input.InputHandler;
import org.example.ascendrix.MainGame.Engine.GameEngine;

public class MovementSystem implements Handling {
    private final MovementConfig config;

    private long dasStartTime = -1;
    private long lastArrTime = -1;
    private int currentDir = 0;
    private long lastSdfTime = -1;

    public MovementSystem(MovementConfig config) {
        this.config = config;
    }

    private void handleHorizontal(long now, int dir, GameEngine game) {
        if (dir != currentDir) {
            currentDir = dir;
            dasStartTime = now;
            lastArrTime = now;
            return;
        }

        if (dir == 0) {
            dasStartTime = -1;
            lastArrTime = -1;
            return;
        }

        // DAS not completed charging
        if (now - dasStartTime < config.dasNs) return;

        // ARR phase
        if (config.instantArr || config.arrNs == 0) {
            while (game.canMoveHorizontal(dir)) {
                game.move(dir);
            }
        } else {
            if (lastArrTime == -1 || now - lastArrTime >= config.arrNs) {
                game.move(dir);
                lastArrTime = now;
            }
        }
    }

    private void handleSDF(long now, InputHandler input, GameEngine game) {
        if (!input.isSoftDropHeld()) {
            lastSdfTime = -1;
            return;
        }

        if (config.instantSdf) {
            while (!game.isOnGround()) {
                game.softDrop(now);
            }
            return;
        }

        if (lastSdfTime == -1 || now - lastSdfTime >= config.sdfNs) {
            game.softDrop(now);
            lastSdfTime = now;
        }
    }

    @Override
    public void update(long now, InputHandler input, GameEngine game) {
        int dir = input.getHorizontal();

        handleHorizontal(now, dir, game);
        handleSDF(now, input, game);

        input.tick();
    }
}