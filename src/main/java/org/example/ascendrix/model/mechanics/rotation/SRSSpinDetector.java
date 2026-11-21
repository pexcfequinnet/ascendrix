package org.example.ascendrix.model.mechanics.rotation;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.piece.ActivePiece;
import org.example.ascendrix.model.piece.TetrominoType;

public class SRSSpinDetector implements PieceSpinHandler {

    @Override
    public SpinType detect(ActivePiece piece, Board board) {
        if (piece == null || board == null) {
            return SpinType.NONE;
        }
        // Must have locked immediately after a rotation with no subsequent shift or drop
        if (!piece.isLastMoveWasRotation() || piece.isMovedAfterRotation()) {
            return SpinType.NONE;
        }

        TetrominoType type = piece.getType();
        if (type == TetrominoType.T) {
            return detectTSpin(piece, board);
        }
        if (type == TetrominoType.O) {
            return SpinType.NONE;
        }

        // TETR.IO All-Mini+ rule: non-T pieces award a Mini Spin when immobile
        return isImmobile(piece, board) ? toMiniSpin(type) : SpinType.NONE;
    }

    /* ========================== T-SPIN (3-CORNER RULE) ========================== */

    private SpinType detectTSpin(ActivePiece piece, Board board) {
        // Center of T's 3x3 SRS bounding box is always local offset (1, 1)
        int cx = piece.getX() + 1;
        int cy = piece.getY() + 1;

        boolean tl = isCornerBlocked(board, cx - 1, cy - 1);
        boolean tr = isCornerBlocked(board, cx + 1, cy - 1);
        boolean bl = isCornerBlocked(board, cx - 1, cy + 1);
        boolean br = isCornerBlocked(board, cx + 1, cy + 1);

        int blockedCorners = count(tl, tr, bl, br);
        if (blockedCorners < 3) {
            return SpinType.NONE;
        }

        // 5th kick test (index 4, e.g., TST / Fin) always upgrades to a full T-Spin
        if (piece.getLastKickIndex() == 4) {
            return SpinType.T_SPIN;
        }

        int frontCorners = switch (Math.floorMod(piece.getRotation(), 4)) {
            case 0 -> count(tl, tr); // Pointing Up
            case 1 -> count(tr, br); // Pointing Right
            case 2 -> count(bl, br); // Pointing Down
            case 3 -> count(tl, bl); // Pointing Left
            default -> 0;
        };

        return (frontCorners == 2) ? SpinType.T_SPIN : SpinType.MINI_T_SPIN;
    }

    /* ========================== ALL-SPINS (IMMOBILE RULE) ========================== */

    private boolean isImmobile(ActivePiece piece, Board board) {
        int[][] blocks = piece.getBlocks();
        int x = piece.getX();
        int y = piece.getY();

        return !board.canPlace(blocks, x - 1, y)
                && !board.canPlace(blocks, x + 1, y)
                && !board.canPlace(blocks, x, y + 1)
                && !board.canPlace(blocks, x, y - 1);
    }

    private static SpinType toMiniSpin(TetrominoType type) {
        return switch (type) {
            case L -> SpinType.MINI_L_SPIN;
            case J -> SpinType.MINI_J_SPIN;
            case S -> SpinType.MINI_S_SPIN;
            case Z -> SpinType.MINI_Z_SPIN;
            case I -> SpinType.MINI_I_SPIN;
            default -> SpinType.NONE;
        };
    }

    /* ========================== HELPERS ========================== */

    public static boolean isCornerBlocked(Board board, int x, int y) {
        if (x < 0 || x >= Board.COLS || y >= Board.ROWS) {
            return true;
        }
        if (y < 0) {
            return false;
        }
        return !board.isCellEmpty(y, x);
    }

    private static int count(boolean a, boolean b) {
        return (a ? 1 : 0) + (b ? 1 : 0);
    }

    private static int count(boolean a, boolean b, boolean c, boolean d) {
        return (a ? 1 : 0) + (b ? 1 : 0) + (c ? 1 : 0) + (d ? 1 : 0);
    }
}