package org.example.ascendrix;

public class TetrominoHandler {

    public int x, y;
    public TetrominoType type;
    public int rotation;
    public int[][] blocks;
    public TetrominoHandler(TetrominoType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.rotation = 0;
        this.blocks = deepCopy(type.blocks);
    }
    public static int[][] deepCopy(int[][] arr) {
        int[][] copy = new int[arr.length][2];
        for (int i = 0; i < arr.length; i++) {
            copy[i][0] = arr[i][0];
            copy[i][1] = arr[i][1];
        }
        return copy;
    }

    public int[][] getBlocks() {
        return blocks;
    }
}