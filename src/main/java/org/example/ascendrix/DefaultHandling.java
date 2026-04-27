package org.example.ascendrix;

public class DefaultHandling implements Handling{
    private final MovementConfig config;

    private int currentDir = 0;
    private long dasStartFrame = -1;
    private long lastArrFrame  = -1;
    private long lastSdfFrame  = -1;

    public DefaultHandling(MovementConfig config) {
        this.config = config;
    }

    private void handleHorizontal(long frame, int dir, GameEngine game) {
        if (dir != currentDir) {
            currentDir = dir;
            dasStartFrame = -1;
            lastArrFrame = -1;
        }

        if (dir == 0) return;

        // First press this direction
        if (dasStartFrame == -1) {
            game.move(dir);
            dasStartFrame = frame;
            return;
        }

        // DAS not yet charged
        if (frame - dasStartFrame < config.dasFrames) return;

        // DAS charged — apply ARR
        if (config.instantArr) {
            while (game.canMoveHorizontal(dir)) game.move(dir);
        } else {
            if (lastArrFrame == -1 || frame - lastArrFrame >= config.arrFrames) {
                game.move(dir);
                lastArrFrame = frame;
            }
        }
    }

    private void handleSDF(long frame, InputHandler input, GameEngine game) {
        if (!input.isSoftDropHeld()) {
            lastSdfFrame = -1;
            return;
        }
        if (config.instantSdf)
            while (!game.isOnGround())
               game.softDrop();

        if (lastSdfFrame == -1 || frame - lastSdfFrame >= config.sdfFrames) {
            game.softDrop();
            lastSdfFrame = frame;
        }
    }
    private void handleActions(InputHandler input, GameEngine game) {
        if (input.isRotateCWJustPressed())  game.rotateCW();
        if (input.isRotateCCWJustPressed()) game.rotateCCW();
        if (input.isHardDropJustPressed())  game.hardDrop();
        if (input.isHoldJustPressed())      game.hold();
    }

    @Override
    public void update(long frame, InputHandler input, GameEngine game) {
        boolean left  = input.isLeftHeld();
        boolean right = input.isRightHeld();

        int dir = 0;
        if (left ^ right) dir = left ? -1 : 1;

        handleHorizontal(frame, dir, game);
        handleSDF(frame, input, game);
        handleActions(input, game);
        input.tick();
    }
}