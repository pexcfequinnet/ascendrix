package org.example.ascendrix.Rotation.SRS;

import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.Rotation.RotationDirection;
import org.example.ascendrix.Rotation.RotationSystem;
import org.example.ascendrix.Tetromino.TetrominoHandler;
import org.example.ascendrix.Tetromino.TetrominoType;

public class StandardRotationSystem implements RotationSystem {
    @Override
    public boolean tryRotate(TetrominoHandler piece, RotationDirection dir, GameEngine game) {
        if (piece.type == TetrominoType.O) return false;

        int from = piece.rotation;
        int to;

        switch (dir) {
            case CW ->
                    to = (from + 1) % 4;

            case CCW ->
                    to = (from + 3) % 4;

            default ->
                    throw new IllegalStateException();
        }

        int[][] kicks =
                SRSKickTable.getKicks(
                        piece.type,
                        from,
                        to
                );
        int[][] newBlocks = rotatedShape(piece.blocks, piece.type, dir);

        for (int i = 0; i < kicks.length; i++) {
            int[] k = kicks[i];

            int newX = piece.x + k[0];
            int newY = piece.y + k[1];

            if (game.canPlace(newBlocks, newX, newY)) {
                piece.applyRotation(newX, newY, to, newBlocks, i, k[0] != 0);
                return true;
            }
        }
        return false;
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
    private static int[] rotateCCW(int x, int y, double px, double py) {
        double relX = x - px;
        double relY = y - py;

        double newX = px + relY;
        double newY = py - relX;

        return new int[]{
                (int)Math.round(newX),
                (int)Math.round(newY)
        };
    }

    private int[][] rotatedShape(
            int[][] currentShape,
            TetrominoType type,
            RotationDirection dir
    ) {
        double[] pivot = getPivot(type);
        int[][] next = new int[currentShape.length][2];

        for (int i = 0; i < currentShape.length; i++) {

            switch (dir) {

                case CW -> next[i] = rotateCW(
                        currentShape[i][0],
                        currentShape[i][1],
                        pivot[0],
                        pivot[1]
                );

                case CCW -> next[i] = rotateCCW(
                        currentShape[i][0],
                        currentShape[i][1],
                        pivot[0],
                        pivot[1]
                );
            }
        }

        return next;
    }
}
