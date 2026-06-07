package org.example.ascendrix.Rotation;

public enum RotationDirection {
    NONE(0),
    CW(1),
    CCW(-1),
    ROTATE_180(2);

    private final int value;

    RotationDirection(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
