package org.example.ascendrix.model.mechanics.rotation;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.piece.ActivePiece;
import org.example.ascendrix.model.piece.TetrominoType;

public class SRS implements RotationSystem {

    private static final double[] PIVOT_I = {1.5, 1.5};
    private static final double[] PIVOT_O = {1.5, 0.5};
    private static final double[] PIVOT_JLSTZ = {1.0, 1.0};

    @Override
    public boolean tryRotate(ActivePiece piece, RotationDirection dir, Board board) {
        if (piece == null || board == null || dir == null || dir == RotationDirection.NONE) {
            return false;
        }
        if (piece.getType() == TetrominoType.O) {
            return false;
        }

        int from = Math.floorMod(piece.getRotation(), 4);
        int to = switch (dir) {
            case CW   -> Math.floorMod(from + 1, 4);
            case CCW  -> Math.floorMod(from + 3, 4);
            case FLIP -> Math.floorMod(from + 2, 4);
            case NONE -> from;
        };

        int[][] kicks = SRSKickTable.getKicks(piece.getType(), from, to);
        int[][] newBlocks = rotatedShape(piece.getBlocks(), piece.getType(), dir);

        for (int i = 0; i < kicks.length; i++) {
            int dx = kicks[i][0];
            int dy = kicks[i][1];
            int newX = piece.getX() + dx;
            int newY = piece.getY() + dy;

            if (board.canPlace(newBlocks, newX, newY)) {
                piece.applyRotation(newX, newY, to, newBlocks, i, dx != 0);
                return true;
            }
        }
        return false;
    }

    public static double[] getPivot(TetrominoType type) {
        return switch (type) {
            case I -> PIVOT_I;
            case O -> PIVOT_O;
            default -> PIVOT_JLSTZ;
        };
    }

    private static int[][] rotatedShape(int[][] currentShape,
                                        TetrominoType type,
                                        RotationDirection dir) {
        double[] pivot = getPivot(type);
        double px = pivot[0];
        double py = pivot[1];
        int[][] next = new int[currentShape.length][2];

        for (int i = 0; i < currentShape.length; i++) {
            int x = currentShape[i][0];
            int y = currentShape[i][1];
            next[i] = switch (dir) {
                case CW   -> rotateCW(x, y, px, py);
                case CCW  -> rotateCCW(x, y, px, py);
                case FLIP -> rotate180(x, y, px, py);
                case NONE -> new int[]{x, y};
            };
        }
        return next;
    }

    private static int[] rotateCW(int x, int y, double px, double py) {
        double relX = x - px;
        double relY = y - py;
        return new int[]{
                (int) Math.round(px - relY),
                (int) Math.round(py + relX)
        };
    }

    private static int[] rotateCCW(int x, int y, double px, double py) {
        double relX = x - px;
        double relY = y - py;
        return new int[]{
                (int) Math.round(px + relY),
                (int) Math.round(py - relX)
        };
    }

    private static int[] rotate180(int x, int y, double px, double py) {
        return new int[]{
                (int) Math.round(2 * px - x),
                (int) Math.round(2 * py - y)
        };
    }
}