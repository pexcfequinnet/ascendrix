package org.example.ascendrix;

import javafx.animation.AnimationTimer;

public class GameEngine {

    private final int COLS = 10;
    private final int ROWS = 20;

    private final int[][] board = new int[ROWS][COLS];

    private Tetromino current;

    private final GameRenderer renderer;

    private long lastUpdate = 0;
    private final long fallDelay = 500_000_000; // 0.5s

    public GameEngine(GameRenderer renderer) {
        this.renderer = renderer;
        spawnBlock();
    }

    public void start() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate > fallDelay) {
                    update();
                    lastUpdate = now;
                }
                renderer.render(board, current);
            }
        }
        .start();
    }

    private void spawnBlock() {
        Tetromino.TetrominoType[] types = Tetromino.TetrominoType.values();
        Tetromino.TetrominoType randomType = types[(int)(Math.random() * types.length)];

        current = new Tetromino(randomType, 3, 0);
    }

    private void update() {
        if (canMove(current.x, current.y + 1)) {
            current.y++;
        } else {
            lockBlock();
            spawnBlock();
        }
    }

    // ===== Movement =====
    public void moveLeft() {
        if (canMove(current.x - 1, current.y)) current.x--;
    }

    public void moveRight() {
        if (canMove(current.x + 1, current.y)) current.x++;
    }

    public void softDrop() {
        if (canMove(current.x, current.y + 1)) current.y++;
    }

    public void hardDrop() {
        int y = current.y;
        while (canMove(current.x, y + 1)) y++;
        current.y = y;
        lockBlock();
        spawnBlock();
    }

    public void rotate() {
        int[][] rotated = current.getRotated();

        if (canPlace(rotated, current.x, current.y)) {
            current.shape = rotated;
            return;
        }

        if (canPlace(rotated, current.x - 1, current.y)) {
            current.x--;
            current.shape = rotated;
        } else if (canPlace(rotated, current.x + 1, current.y)) {
            current.x++;
            current.shape = rotated;
        }
    }

    // ===== Logic =====
    private boolean canMove(int newX, int newY) {
        return canPlace(current.shape, newX, newY);
    }

    private boolean canPlace(int[][] shape, int newX, int newY) {
        for (int[] p : shape) {
            int x = newX + p[0];
            int y = newY + p[1];

            if (x < 0 || x >= COLS || y >= ROWS) return false;
            if (y >= 0 && board[y][x] != 0) return false;
        }
        return true;
    }

    private void lockBlock() {
        for (int[] p : current.shape) {
            int x = current.x + p[0];
            int y = current.y + p[1];
            if (y >= 0) board[y][x] = 1;
        }
    }
}