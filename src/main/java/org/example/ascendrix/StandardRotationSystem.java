package org.example.ascendrix;

public class StandardRotationSystem implements RotationSystem {
    @Override
    public void tryRotate(TetrominoHandler piece, int dir, GameEngine game) {
        if (piece.type == TetrominoType.O) return;

        int from = piece.rotation;
        int to = (from + dir + 4) % 4;

        int[][] kicks = getKicks(piece.type, from, to);
        int[][] newBlocks = rotatedShape(piece.blocks, piece.type, dir);

        for (int i = 0; i < kicks.length; i++) {
            int[] k = kicks[i];

            int newX = piece.x + k[0];
            int newY = piece.y + k[1];

            if (game.canPlace(newBlocks, newX, newY)) {
                piece.applyRotation(newX, newY, to, newBlocks, i, k[0] != 0);
                return;
            }
        }
    }
    // JLSTZ piece kick table: https://harddrop.com/wiki/SRS#Wall_kicks
    /* Flipping all y values since JavaFX increase y value downward */
    private static int[][] getJLSTZ(int from, int to) {
        // CW
        if (from == 0 && to == 1) return new int[][]{{0,0},{-1,0},{-1,-1},{0,2},{-1,2}};
        if (from == 1 && to == 2) return new int[][]{{0,0},{1,0},{1,1},{0,-2},{1,-2}};
        if (from == 2 && to == 3) return new int[][]{{0,0},{1,0},{1,-1},{0,2},{1,2}};
        if (from == 3 && to == 0) return new int[][]{{0,0},{-1,0},{-1,1},{0,-2},{-1,-2}};

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
        if (from == 0 && to == 1) return new int[][]{{0,0},{-2,0},{1,0},{-2,1},{1,-2}};
        if (from == 1 && to == 2) return new int[][]{{0,0},{-1,0},{2,0},{-1,-2},{2,1}};
        if (from == 2 && to == 3) return new int[][]{{0,0},{2,0},{-1,0},{2,-1},{-1,2}};
        if (from == 3 && to == 0) return new int[][]{{0,0},{1,0},{-2,0},{1,2},{-2,-1}};

        // CCW
        if (from == 1 && to == 0) return new int[][]{{0,0},{2,0},{-1,0},{2,-1},{-1,2}};
        if (from == 2 && to == 1) return new int[][]{{0,0},{1,0},{-2,0},{1,2},{-2,-1}};
        if (from == 3 && to == 2) return new int[][]{{0,0},{-2,0},{1,0},{-2,1},{1,-2}};
        if (from == 0 && to == 3) return new int[][]{{0,0},{-1,0},{2,0},{-1,-2},{2,1}};

        return new int[][]{{0,0}};
    }

    // Pivot:
    public static double[] getPivot(TetrominoType type) {
        return switch (type) {
            case I -> new double[]{1.5, 1.5};
            case O -> new double[]{1.5, 0.5};
            default -> new double[]{1, 1}; // JLSTZ
        };
    }

    private static int[] rotateCW(int x, int y, double px, double py) {
        double relX = x - px;
        double relY = y - py;

        double newX = px - relY;
        double newY = py + relX;

        return new int[]{
                (int)Math.round(newX),
                (int)Math.round(newY)
        };
    }
    private int[] rotateCCW(int x, int y, double px, double py) {
        double relX = x - px;
        double relY = y - py;

        double newX = px + relY;
        double newY = py - relX;

        return new int[]{
                (int)Math.round(newX),
                (int)Math.round(newY)
        };
    }

    private int[][] rotatedShape(int[][] currentShape, TetrominoType type, int dir) {
        double[] pivot = getPivot(type);
        int[][] next = new int[currentShape.length][2];

        for (int i = 0; i < currentShape.length; i++) {
            if (dir == 1) {
                next[i] = rotateCW(
                        currentShape[i][0],
                        currentShape[i][1],
                        pivot[0],
                        pivot[1]
                );
            } else {
                next[i] = rotateCCW(
                        currentShape[i][0],
                        currentShape[i][1],
                        pivot[0],
                        pivot[1]
                );
            }
        }

        return next;
    }


    private static int[][] getKicks(TetrominoType type, int from, int to) {
        if (type == TetrominoType.O) {
            return new int[][]{{0, 0}};
        }
        if (type == TetrominoType.I) {
            return getI(from, to);
        }
        return getJLSTZ(from, to);
    }
}
