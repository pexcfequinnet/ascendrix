package org.example.ascendrix;

public enum TetrominoType {

    I(new int[][]{
            {0, 1}, {1, 1}, {2, 1}, {3, 1}
    }),

    O(new int[][]{
            {1, 0}, {2, 0},
            {1, 1}, {2, 1}
    }),

    T(new int[][]{
            {1, 0},
            {0, 1}, {1, 1}, {2, 1}
    }),

    S(new int[][]{
            {1, 0}, {2, 0},
            {0, 1}, {1, 1}
    }),

    Z(new int[][]{
            {0, 0}, {1, 0},
            {1, 1}, {2, 1}
    }),

    J(new int[][]{
            {0, 0},
            {0, 1}, {1, 1}, {2, 1}
    }),

    L(new int[][]{
            {2, 0},
            {0, 1}, {1, 1}, {2, 1}
    });

    public final int[][] shape;

    TetrominoType(int[][] shape) {
        this.shape = shape;
    }
}
