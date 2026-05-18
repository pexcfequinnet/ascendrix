package org.example.ascendrix.Rotation.SRS;


import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.Rotation.PieceSpinHandler;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoHandler;

public class SRSSpinDetector implements PieceSpinHandler {

    private boolean isValidSpin(TetrominoHandler t, GameEngine game) {
        return isImmobile(t, game)
                && (t.movedBeforeRotation || t.kickWasHorizontal);
    }
    @Override
    public SpinType detect(TetrominoHandler t, GameEngine game) {
        if (!t.lastMoveWasRotation || t.movedAfterRotation)
            return SpinType.NONE;

        double[] pivot = StandardRotationSystem.getPivot(t.type);
        int cx = (int) Math.round(t.x + pivot[0]);
        int cy = (int) Math.round(t.y + pivot[1]);

        boolean tl = isBlocked(game, cx - 1, cy - 1);
        boolean tr = isBlocked(game, cx + 1, cy - 1);
        boolean bl = isBlocked(game, cx - 1, cy + 1);
        boolean br = isBlocked(game, cx + 1, cy + 1);

        return switch (t.type) {
            case T -> {
                int corners = count(tl, tr, bl, br);
                boolean immobile = !game.canPlace(t.blocks, t.x - 1, t.y) && !game.canPlace(t.blocks, t.x + 1, t.y);
                if (!immobile) yield SpinType.NONE;
                if (t.fellBetweenRotations) yield SpinType.NONE;
                if (corners < 3 && t.lastKickIndex == 0 && !t.movedBeforeRotation) yield SpinType.NONE;
                yield classifyTSpin(t, tl, tr, bl, br, corners);
            }
            case L -> isValidSpin(t, game) ? SpinType.MINI_L_SPIN : SpinType.NONE;
            case J -> isValidSpin(t, game) ? SpinType.MINI_J_SPIN : SpinType.NONE;
            case S -> isValidSpin(t, game) ? SpinType.MINI_S_SPIN : SpinType.NONE;
            case Z -> isValidSpin(t, game) ? SpinType.MINI_Z_SPIN : SpinType.NONE;
            case I -> isValidSpin(t, game) ? SpinType.MINI_I_SPIN : SpinType.NONE;
            default -> SpinType.NONE;
        };
    }

    private boolean isImmobile(TetrominoHandler t, GameEngine game) {
        return !game.canPlace(t.blocks, t.x - 1, t.y)
                && !game.canPlace(t.blocks, t.x + 1, t.y)
                && !game.canPlace(t.blocks, t.x, t.y + 1);
    }

    public static boolean isBlocked(GameEngine game, int x, int y) {
        if (x < 0 || x >= game.COLS || y >= game.ROWS) return true;
        if (y < 0) return false;
        return game.board[y][x] != null;
    }

    private static SpinType classifyTSpin(TetrominoHandler t, boolean tl, boolean tr, boolean bl, boolean br, int corners) {
        if (t.lastKickIndex == 3) return SpinType.T_SPIN;

        boolean[] front = switch (t.rotation) {
            case 0 -> new boolean[]{ tl, tr };
            case 1 -> new boolean[]{ tr, br };
            case 2 -> new boolean[]{ bl, br };
            case 3 -> new boolean[]{ tl, bl };
            default -> new boolean[]{};
        };

        int f = count(front);

        if (t.lastKickIndex == 4)                          return SpinType.T_SPIN;
        if (f == 2 && corners >= 3)                        return SpinType.T_SPIN;
        if (f == 2)                                        return SpinType.NONE;
        if (f == 1)                                        return SpinType.MINI_T_SPIN;
        if (corners >= 1 && t.movedBeforeRotation)         return SpinType.MINI_T_SPIN;
        return SpinType.NONE;
    }
    private static int count(boolean... flags) {
        int n = 0;
        for (boolean f : flags) if (f) n++;
        return n;
    }
}