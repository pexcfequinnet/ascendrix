package org.example.ascendrix.model.mechanics.rotation;

import org.example.ascendrix.model.piece.TetrominoType;

public final class SRSKickTable {

    private static final int[][] NO_KICK = {{0, 0}};

    // Pre-inverted lookup tables indexed by (from * 4 + to)
    private static final int[][][] JLSTZ_TABLE = new int[16][][];
    private static final int[][][] I_TABLE = new int[16][][];

    static {
        for (int i = 0; i < 16; i++) {
            JLSTZ_TABLE[i] = NO_KICK;
            I_TABLE[i] = NO_KICK;
        }

        // Raw Guideline SRS JLSTZ wall kicks (+Y = up, matching Hard Drop Wiki)
        // CW
        register(JLSTZ_TABLE, 0, 1, new int[][]{{0, 0}, {-1, 0}, {-1,  1}, {0, -2}, {-1, -2}});
        register(JLSTZ_TABLE, 1, 2, new int[][]{{0, 0}, { 1, 0}, { 1, -1}, {0,  2}, { 1,  2}});
        register(JLSTZ_TABLE, 2, 3, new int[][]{{0, 0}, { 1, 0}, { 1,  1}, {0, -2}, { 1, -2}});
        register(JLSTZ_TABLE, 3, 0, new int[][]{{0, 0}, {-1, 0}, {-1, -1}, {0,  2}, {-1,  2}});
        // CCW
        register(JLSTZ_TABLE, 1, 0, new int[][]{{0, 0}, { 1, 0}, { 1, -1}, {0,  2}, { 1,  2}});
        register(JLSTZ_TABLE, 2, 1, new int[][]{{0, 0}, {-1, 0}, {-1,  1}, {0, -2}, {-1, -2}});
        register(JLSTZ_TABLE, 3, 2, new int[][]{{0, 0}, {-1, 0}, {-1, -1}, {0,  2}, {-1,  2}});
        register(JLSTZ_TABLE, 0, 3, new int[][]{{0, 0}, { 1, 0}, { 1,  1}, {0, -2}, { 1, -2}});

        // Raw Guideline SRS I-piece wall kicks (+Y = up, matching Hard Drop Wiki)
        // CW
        register(I_TABLE, 0, 1, new int[][]{{0, 0}, {-2, 0}, { 1, 0}, {-2, -1}, { 1,  2}});
        register(I_TABLE, 1, 2, new int[][]{{0, 0}, {-1, 0}, { 2, 0}, {-1,  2}, { 2, -1}});
        register(I_TABLE, 2, 3, new int[][]{{0, 0}, { 2, 0}, {-1, 0}, { 2,  1}, {-1, -2}});
        register(I_TABLE, 3, 0, new int[][]{{0, 0}, { 1, 0}, {-2, 0}, { 1, -2}, {-2,  1}});
        // CCW
        register(I_TABLE, 1, 0, new int[][]{{0, 0}, { 2, 0}, {-1, 0}, { 2,  1}, {-1, -2}});
        register(I_TABLE, 2, 1, new int[][]{{0, 0}, { 1, 0}, {-2, 0}, { 1, -2}, {-2,  1}});
        register(I_TABLE, 3, 2, new int[][]{{0, 0}, {-2, 0}, { 1, 0}, {-2, -1}, { 1,  2}});
        register(I_TABLE, 0, 3, new int[][]{{0, 0}, {-1, 0}, { 2, 0}, {-1,  2}, { 2, -1}});
    }

    private SRSKickTable() {}

    public static int[][] getKicks(TetrominoType type, int from, int to) {
        if (type == TetrominoType.O) {
            return NO_KICK;
        }
        int index = Math.floorMod(from, 4) * 4 + Math.floorMod(to, 4);
        return (type == TetrominoType.I) ? I_TABLE[index] : JLSTZ_TABLE[index];
    }

    private static void register(int[][][] table, int from, int to, int[][] rawKicks) {
        table[from * 4 + to] = invertY(rawKicks);
    }

    private static int[][] invertY(int[][] raw) {
        int[][] inverted = new int[raw.length][2];
        for (int i = 0; i < raw.length; i++) {
            inverted[i][0] = raw[i][0];
            inverted[i][1] = -raw[i][1];
        }
        return inverted;
    }
}