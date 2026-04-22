package org.example.ascendrix;

import javafx.animation.AnimationTimer;

public class GameEngine {
    private final int COLS = 10;
    private final int ROWS = 20;
    private final int[][] board = new int[ROWS][COLS];
    private TetrominoHandler current;
    private TetrominoQueue queue;
    private double score = 0;
    private int totalLines = 0;
    private final GameRenderer renderer;
    private long lastUpdate = 0;
    private final long fallDelay = 500_000_000; // 0.5s

    public GameEngine(GameRenderer renderer) {
        this.renderer = renderer;
        queue = new TetrominoQueue();
        System.out.println(queue.getPreview());

        spawnBlock();
    }
    public void start() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (current == null) return;
                if (now - lastUpdate > fallDelay) {
                    update();
                    lastUpdate = now;
                }
                renderer.render(board, current);
                renderer.renderNext(queue.getPreview());

            }
        }
        .start();
    }
    private void update() {
        if (canMove(current.x, current.y + 1)) {
            current.y++;
        } else {
            lockBlock();
            int cleared = clearLines();
            System.out.println("Cleared = " + cleared);
            totalLines += cleared;

            score += switch (cleared) {
                case 1 -> 0.1;
                case 2 -> 0.2;
                case 3 -> 0.3;
                case 4 -> 0.5;
                default -> 0;
            };
            spawnBlock();
        }
    }
    // Vertical piece offset
    private int getSpawnYOffset(TetrominoType type) {
        return switch (type) {
            case I -> -1;
            default -> 0;
        };
    }

    private void spawnBlock() {
        TetrominoType type = queue.next();

        TetrominoHandler piece = new TetrominoHandler(type, 3, getSpawnYOffset(type));
        piece.y = getSpawnYOffset(type);

        current = piece;
    }

    // Line clear system
    private int clearLines() {
        int linesCleared = 0;
        int y = ROWS - 1;

        while (y >= 0) {
            boolean full = true;

            for (int x = 0; x < COLS; x++) {
                if (board[y][x] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                removeLine(y);
                linesCleared++;
            } else {
                y--;
            }
        }

        return linesCleared;
    }
    private void removeLine(int line) {
        for (int y = line; y > 0; y--) {
            for (int x = 0; x < COLS; x++) {
                board[y][x] = board[y - 1][x];
            }
        }

        // Clear upper line
        for (int x = 0; x < COLS; x++) {
            board[0][x] = 0;
        }
    }
    // Movement
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

        int cleared = clearLines();

        totalLines += cleared;

        score += switch (cleared) {
            case 1 -> 0.1;
            case 2 -> 0.2;
            case 3 -> 0.3;
            case 4 -> 0.5;
            default -> 0;
        };

        spawnBlock();
    }

    public void rotateCW() {
        int[][] rotated = current.getRotatedCW();

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
    public void rotateCCW() {
        int[][] rotated = current.getRotatedCCW();

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

    // Logic
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
            if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                board[y][x] = 1;
            }
        }
    }
}