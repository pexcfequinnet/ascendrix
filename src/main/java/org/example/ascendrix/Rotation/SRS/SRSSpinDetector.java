package org.example.ascendrix.Rotation.SRS;


import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.Rotation.PieceSpinHandler;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.TetrominoHandler;

public class SRSSpinDetector implements PieceSpinHandler {

    @Override
    public SpinType detect(TetrominoHandler t, GameEngine game) {
        // ĐIỀU KIỆN 1: Hành động cuối cùng bắt buộc phải là Xoay (Rotation)
        // và không bị dịch chuyển sang ngang/rơi xuống sau khi xoay.
        if (!t.lastMoveWasRotation || t.movedAfterRotation) {
            return SpinType.NONE;
        }

        double[] pivot = StandardRotationSystem.getPivot(t.type);
        int cx = (int) Math.round(t.x + pivot[0]);
        int cy = (int) Math.round(t.y + pivot[1]);

        // Kiểm tra 4 ô đường chéo xung quanh tâm xoay (Pivot)
        boolean tl = isBlocked(game, cx - 1, cy - 1); // Trái-Trên
        boolean tr = isBlocked(game, cx + 1, cy - 1); // Phải-Trên
        boolean bl = isBlocked(game, cx - 1, cy + 1); // Trái-Dưới
        boolean br = isBlocked(game, cx + 1, cy + 1); // Phải-Dưới

        return switch (t.type) {
            case T -> {
                int corners = count(tl, tr, bl, br);
                int frontCorners = countFront(t.rotation, tl, tr, bl, br);
                yield classifyT(t, corners, frontCorners);
            }
            case L -> classifyOther(t, game) ? SpinType.MINI_L_SPIN : SpinType.NONE;
            case J -> classifyOther(t, game) ? SpinType.MINI_J_SPIN : SpinType.NONE;
            case S -> classifyOther(t, game) ? SpinType.MINI_S_SPIN : SpinType.NONE;
            case Z -> classifyOther(t, game) ? SpinType.MINI_Z_SPIN : SpinType.NONE;
            case I -> classifyOther(t, game) ? SpinType.MINI_I_SPIN : SpinType.NONE;
            default -> SpinType.NONE;
        };
    }

// ---- T-spin (Áp dụng luật 3 góc chuẩn SRS) ----

    private SpinType classifyT(TetrominoHandler t, int corners, int frontCorners) {
        // ĐIỀU KIỆN 2: Phải có ít nhất 3 góc bị chặn bởi tường/đáy hoặc gạch khác
        if (corners < 3) return SpinType.NONE;

        // ĐIỀU KIỆN 3: Kick thứ 5 (Index 4) luôn luôn là T-Spin Full
        // (Thường dùng cho T-Spin Triple hoặc Fin/Neo T-Spin)
        if (t.lastKickIndex == 4) return SpinType.T_SPIN;

        // ĐIỀU KIỆN 4: Kiểm tra số góc phía trước mũi chữ T
        if (frontCorners == 2) {
            return SpinType.T_SPIN;      // Cả 2 góc trước bị chặn -> T-Spin xịn
        } else {
            return SpinType.MINI_T_SPIN; // Chỉ 1 góc trước bị chặn -> Mini T-Spin
        }
    }

    private static int countFront(int rotation, boolean tl, boolean tr, boolean bl, boolean br) {
        // Xác định 2 góc nằm ở phần "mặt phẳng" của khối T (Mũi T chỉa đi đâu thì góc trước ở đó)
        return switch (rotation) {
            case 0 -> count(tl, tr); // Chĩa lên -> Trái-Trên, Phải-Trên
            case 1 -> count(tr, br); // Chĩa phải -> Phải-Trên, Phải-Dưới
            case 2 -> count(bl, br); // Chĩa xuống -> Trái-Dưới, Phải-Dưới
            case 3 -> count(tl, bl); // Chĩa trái -> Trái-Trên, Trái-Dưới
            default -> 0;
        };
    }

// ---- Non-T spins (All-Spins) ----

    private boolean classifyOther(TetrominoHandler t, GameEngine game) {
        // Luật Immobile (Kẹt cứng): Khối gạch bị bao vây đến mức không thể nhúc nhích
        // theo bất kỳ hướng nào (Lên, Xuống, Trái, Phải) nếu không dùng thao tác Xoay.
        return !game.canPlace(t.blocks, t.x - 1, t.y)      // Kẹt Trái
                && !game.canPlace(t.blocks, t.x + 1, t.y)      // Kẹt Phải
                && !game.canPlace(t.blocks, t.x, t.y + 1)      // Kẹt Dưới
                && !game.canPlace(t.blocks, t.x, t.y - 1);     // Kẹt Trên
    }

// ---- Helpers ----

    public static boolean isBlocked(GameEngine game, int x, int y) {
        // Tường 2 bên và đáy luôn được coi là "bị chặn"
        if (x < 0 || x >= game.COLS || y >= game.ROWS) return true;
        // Khoảng không trên trần nhà không bị chặn
        if (y < 0) return false;
        // Kiểm tra xem ô đó có gạch chết chưa
        return game.board[y][x] != null;
    }

    private static int count(boolean... flags) {
        int n = 0;
        for (boolean f : flags) if (f) n++;
        return n;
    }
}