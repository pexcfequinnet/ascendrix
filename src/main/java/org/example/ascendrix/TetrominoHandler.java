package org.example.ascendrix;

public class TetrominoHandler {

    public int x, y;
    public TetrominoType type;
    public int rotationState = 0;

    public TetrominoHandler(TetrominoType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public int[][] getShape() {
        return type.shapes[rotationState];
    }
}