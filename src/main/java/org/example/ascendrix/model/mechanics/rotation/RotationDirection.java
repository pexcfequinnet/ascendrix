package org.example.ascendrix.model.mechanics.rotation;

public enum RotationDirection {
    NONE(0),
    CW(1),
    CCW(-1),
    FLIP(2);

    private final int value;

    RotationDirection(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}