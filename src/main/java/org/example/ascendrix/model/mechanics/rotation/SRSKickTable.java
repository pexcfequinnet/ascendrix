package org.example.ascendrix.model.mechanics.rotation;

import org.example.ascendrix.model.piece.TetrominoType;

public final class SRSKickTable {

    private static final int[][] NO_KICK = {{0, 0}};

    // JLSTZ CW / CCW (Y inverted for downward-increasing board rows)
    private static final int[][] JLSTZ_0_1 = {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}};
    private static final int[][] JLSTZ_1_2 = {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}};
    private static final int[][] JLSTZ_2_3 = {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}};
    private static final int[][] JLSTZ_3_0 = {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}};

    private static final int[][] JLSTZ_1_0 = {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}};
    private static final int[][] JLSTZ_2_1 = {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}};
    private static final int[][] JLSTZ_3_2 = {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}};
    private static final int[][] JLSTZ_0_3 = {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}};

    // I CW / CCW (Y inverted for downward-increasing board rows)
    private static final int[][] I_0_1 = {{0, 0}, {-2, 0}, {1, 0}, {-2, 1}, {1, -2}};
    private static final int[][] I_1_2 = {{0, 0}, {-1, 0}, {2, 0}, {-1, -2}, {2, 1}};
    private static final int[][] I_2_3 = {{0, 0}, {2, 0}, {-1, 0}, {2, -1}, {-1, 2}};
    private static final int[][] I_3_0 = {{0, 0}, {1, 0}, {-2, 0}, {1, 2}, {-2, -1}};

    private static final int[][] I_1_0 = {{0, 0}, {2, 0}, {-1, 0}, {2, -1}, {-1, 2}};
    private static final int[][] I_2_1 = {{0, 0}, {1, 0}, {-2, 0}, {1, 2}, {-2, -1}};
    private static final int[][] I_3_2 = {{0, 0}, {-2, 0}, {1, 0}, {-2, 1}, {1, -2}};
    private static final int[][] I_0_3 = {{0, 0}, {-1, 0}, {2, 0}, {-1, -2}, {2, 1}};

    // TETR.IO SRS 180° Kick Table (shared across I, J, L, S, T, Z; Y inverted)
    private static final int[][] KICK_180_0_2 = {{0, 0}, {0, -1}, {1, -1}, {-1, -1}, {1, 0}, {-1, 0}};
    private static final int[][] KICK_180_1_3 = {{0, 0}, {1, 0}, {1, -2}, {1, -1}, {0, -2}, {0, -1}};
    private static final int[][] KICK_180_2_0 = {{0, 0}, {0, 1}, {-1, 1}, {1, 1}, {-1, 0}, {1, 0}};
    private static final int[][] KICK_180_3_1 = {{0, 0}, {-1, 0}, {-1, -2}, {-1, -1}, {0, -2}, {0, -1}};

    private SRSKickTable() {}

    public static int[][] getKicks(TetrominoType type, int from, int to) {
        if (type == null || type == TetrominoType.O) {
            return NO_KICK;
        }

        int f = Math.floorMod(from, 4);
        int t = Math.floorMod(to, 4);

        // 180° transitions
        if (t == Math.floorMod(f + 2, 4)) {
            return get180(f);
        }

        return (type == TetrominoType.I) ? getI(f, t) : getJLSTZ(f, t);
    }

    private static int[][] get180(int from) {
        return switch (from) {
            case 0 -> KICK_180_0_2;
            case 1 -> KICK_180_1_3;
            case 2 -> KICK_180_2_0;
            case 3 -> KICK_180_3_1;
            default -> NO_KICK;
        };
    }

    private static int[][] getJLSTZ(int from, int to) {
        if (from == 0 && to == 1) return JLSTZ_0_1;
        if (from == 1 && to == 2) return JLSTZ_1_2;
        if (from == 2 && to == 3) return JLSTZ_2_3;
        if (from == 3 && to == 0) return JLSTZ_3_0;

        if (from == 1 && to == 0) return JLSTZ_1_0;
        if (from == 2 && to == 1) return JLSTZ_2_1;
        if (from == 3 && to == 2) return JLSTZ_3_2;
        if (from == 0 && to == 3) return JLSTZ_0_3;

        return NO_KICK;
    }

    private static int[][] getI(int from, int to) {
        if (from == 0 && to == 1) return I_0_1;
        if (from == 1 && to == 2) return I_1_2;
        if (from == 2 && to == 3) return I_2_3;
        if (from == 3 && to == 0) return I_3_0;

        if (from == 1 && to == 0) return I_1_0;
        if (from == 2 && to == 1) return I_2_1;
        if (from == 3 && to == 2) return I_3_2;
        if (from == 0 && to == 3) return I_0_3;

        return NO_KICK;
    }
}