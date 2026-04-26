package org.example.ascendrix;
// Gravity config for Sprint
public class SprintGravity implements Gravity{

    private long lastFallFrame = -1;
    private final int fallFrames;

    public SprintGravity(int fallFrames) {
        this.fallFrames = fallFrames;
    }

    @Override
    public void update(long frame, GameEngine game) {
        if (lastFallFrame == -1) lastFallFrame = frame;

        if (frame - lastFallFrame >= fallFrames) {
            game.tryFall();
            lastFallFrame = frame;
        }
    }
}
