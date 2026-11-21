package org.example.ascendrix.model.board;

import org.example.ascendrix.model.piece.TetrominoType;

import java.util.Arrays;

public class Board {
    public static final int COLS = 10;
    public static final int ROWS = 25;

    private final TetrominoType[][] grid = new TetrominoType[ROWS][COLS];
    private final boolean[][] garbageMap = new boolean[ROWS][COLS];
    private final boolean[][] boneMap = new boolean[ROWS][COLS];
    private FadeMap fadeMap;

    public void setFadeMap(FadeMap fadeMap) {
        this.fadeMap = fadeMap;
    }

    public FadeMap getFadeMap() {
        return fadeMap;
    }

    public boolean isFading() {
        return fadeMap != null && fadeMap.isFadingBoard();
    }

    public void updateFade(long now) {
        if (fadeMap != null) {
            fadeMap.updateBoardFade(now, grid);
            for (int y = 0; y < ROWS; y++) {
                for (int x = 0; x < COLS; x++) {
                    if (grid[y][x] == null) {
                        boneMap[y][x] = false;
                    }
                }
            }
        }
    }

    public boolean canPlace(int[][] shape, int targetX, int targetY) {
        for (int[] p : shape) {
            int x = targetX + p[0];
            int y = targetY + p[1];
            if (x < 0 || x >= COLS || y >= ROWS) return false;
            if (y >= 0 && isOccupied(y, x)) return false;
        }
        return true;
    }

    public void lockBlocks(int[][] shape, int originX, int originY, TetrominoType type) {
        lockBlocks(shape, originX, originY, type, false);
    }

    public void lockBlocks(int[][] shape, int originX, int originY, TetrominoType type, boolean bone) {
        for (int[] p : shape) {
            int x = originX + p[0];
            int y = originY + p[1];
            if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                grid[y][x] = type;
                garbageMap[y][x] = false;
                boneMap[y][x] = bone;
            }
        }
    }

    public int clearFullLines() {
        int linesCleared = 0;
        int writeY = ROWS - 1;

        for (int readY = ROWS - 1; readY >= 0; readY--) {
            if (isRowFull(readY)) {
                linesCleared++;
            } else {
                System.arraycopy(grid[readY], 0, grid[writeY], 0, COLS);
                System.arraycopy(garbageMap[readY], 0, garbageMap[writeY], 0, COLS);
                System.arraycopy(boneMap[readY], 0, boneMap[writeY], 0, COLS);
                if (fadeMap != null) {
                    fadeMap.copyRow(readY, writeY);
                }
                writeY--;
            }
        }

        for (int y = 0; y <= writeY; y++) {
            Arrays.fill(grid[y], null);
            Arrays.fill(garbageMap[y], false);
            Arrays.fill(boneMap[y], false);
        }

        if (fadeMap != null && linesCleared > 0) {
            fadeMap.clearTopRows(linesCleared);
        }

        return linesCleared;
    }

    public boolean pushUp() {
        for (int col = 0; col < COLS; col++) {
            if (isOccupied(0, col)) {
                return false;
            }
        }

        for (int row = 0; row < ROWS - 1; row++) {
            System.arraycopy(grid[row + 1], 0, grid[row], 0, COLS);
            System.arraycopy(garbageMap[row + 1], 0, garbageMap[row], 0, COLS);
            System.arraycopy(boneMap[row + 1], 0, boneMap[row], 0, COLS);
        }
        Arrays.fill(grid[ROWS - 1], null);
        Arrays.fill(garbageMap[ROWS - 1], false);
        Arrays.fill(boneMap[ROWS - 1], false);
        return true;
    }

    public void setGarbageCell(int row, int col) {
        grid[row][col] = null;
        garbageMap[row][col] = true;
        boneMap[row][col] = false;
    }

    public boolean isGarbageCell(int row, int col) {
        return garbageMap[row][col];
    }

    public boolean isBoneCell(int row, int col) {
        return boneMap[row][col];
    }

    public boolean isCellEmpty(int row, int col) {
        return !isOccupied(row, col);
    }

    public boolean isColumnEmpty(int col) {
        for (int row = 0; row < ROWS; row++) {
            if (isOccupied(row, col)) return false;
        }
        return true;
    }

    public boolean isEmpty() {
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (isOccupied(y, x)) return false;
            }
        }
        return true;
    }

    public TetrominoType getCell(int row, int col) {
        return grid[row][col];
    }

    public TetrominoType[][] getRawGrid() {
        return grid;
    }

    private boolean isOccupied(int row, int col) {
        return grid[row][col] != null || garbageMap[row][col];
    }

    private boolean isRowFull(int row) {
        for (int x = 0; x < COLS; x++) {
            if (!isOccupied(row, x)) return false;
        }
        return true;
    }
}