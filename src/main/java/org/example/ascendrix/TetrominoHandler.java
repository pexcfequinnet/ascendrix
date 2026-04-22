package org.example.ascendrix;


public class TetrominoHandler {


    public int x, y;
    public int[][] shape;
    public TetrominoType type;


    public TetrominoHandler(TetrominoType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.shape = copyShape(type.shape);
    }


    private int[][] copyShape(int[][] src) {
        int[][] copy = new int[src.length][2];
        for (int i = 0; i < src.length; i++) {
            copy[i][0] = src[i][0];
            copy[i][1] = src[i][1];
        }
        return copy;
    }
    private int[][] normalize(int[][] shape) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (int[] p : shape) {
            minX = Math.min(minX, p[0]);
            minY = Math.min(minY, p[1]);
        }

        for (int[] p : shape) {
            p[0] -= minX;
            p[1] -= minY;
        }

        return shape;
    }
    // Rotation
    public int[][] getRotatedCW() {
        int[][] rotated = new int[shape.length][2];

        for (int i = 0; i < shape.length; i++) {
            int x = shape[i][0];
            int y = shape[i][1];

            // Rotate 90 degree: (x, y) → (y, -x)
            rotated[i][0] = y;
            rotated[i][1] = -x;
        }

        return normalize(rotated);
    }
    public int[][] getRotatedCCW() {
        int[][] rotated = new int[shape.length][2];

        for (int i = 0; i < shape.length; i++) {
            int x = shape[i][0];
            int y = shape[i][1];

            rotated[i][0] = y;
            rotated[i][1] = -x;
        }

        return rotated;
    }
}
