package org.example.ascendrix.model.mechanics.rotation;

public enum RotationState {
    SPAWN,   // 0
    RIGHT,   // 1 (R)
    REVERSE, // 2 (2)
    LEFT;    // 3 (L)

    public RotationState rotate(RotationDirection dir) {
        int step = switch (dir) {
            case CW -> 1;
            case CCW -> -1;
            case FLIP -> 2;
            case NONE -> 0;
        };
        RotationState[] states = values();
        return states[Math.floorMod(ordinal() + step, states.length)];
    }
}