package org.example.ascendrix;

public class RotationSRS {
    // JLSTZ kick table: https://harddrop.com/wiki/SRS#Wall_kicks
    private static int[][] getJLSTZ(int from, int to) {
        // CW
        if (from == 0 && to == 1) return new int[][]{{0,0},{-1,0},{-1,1},{0,-2},{-1,-2}};
        if (from == 1 && to == 2) return new int[][]{{0,0},{1,0},{1,-1},{0,2},{1,2}};
        if (from == 2 && to == 3) return new int[][]{{0,0},{1,0},{1,1},{0,-2},{1,-2}};
        if (from == 3 && to == 0) return new int[][]{{0,0},{-1,0},{-1,-1},{0,2},{-1,2}};

        // CCW
        if (from == 1 && to == 0) return new int[][]{{0,0},{1,0},{1,1},{0,-2},{1,-2}};
        if (from == 2 && to == 1) return new int[][]{{0,0},{-1,0},{-1,-1},{0,2},{-1,2}};
        if (from == 3 && to == 2) return new int[][]{{0,0},{-1,0},{-1,1},{0,-2},{-1,-2}};
        if (from == 0 && to == 3) return new int[][]{{0,0},{1,0},{1,-1},{0,2},{1,2}};

        return new int[][]{{0,0}};
    }
    // I piece kick table: https://harddrop.com/wiki/SRS#Wall_kicks
    private static int[][] getI(int from, int to) {
        // CW
        if (from == 0 && to == 1) return new int[][]{{0,0},{-2,0},{1,0},{-2,-1},{1,2}};
        if (from == 1 && to == 2) return new int[][]{{0,0},{-1,0},{2,0},{-1,2},{2,-1}};
        if (from == 2 && to == 3) return new int[][]{{0,0},{2,0},{-1,0},{2,1},{-1,-2}};
        if (from == 3 && to == 0) return new int[][]{{0,0},{1,0},{-2,0},{1,-2},{-2,1}};

        // CCW
        if (from == 1 && to == 0) return new int[][]{{0,0},{2,0},{-1,0},{2,1},{-1,-2}};
        if (from == 2 && to == 1) return new int[][]{{0,0},{1,0},{-2,0},{1,-2},{-2,1}};
        if (from == 3 && to == 2) return new int[][]{{0,0},{-2,0},{1,0},{-2,-1},{1,2}};
        if (from == 0 && to == 3) return new int[][]{{0,0},{-1,0},{2,0},{-1,2},{2,-1}};

        return new int[][]{{0,0}};
    }
    public static int[][] getKicks(TetrominoType type, int from, int to) {
        if (type == TetrominoType.I) {
            return getI(from, to);
        }
        return getJLSTZ(from, to);
    }
}
