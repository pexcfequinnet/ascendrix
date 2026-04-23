package org.example.ascendrix;

import javafx.animation.AnimationTimer;

public class GameEngine {
    private final int COLS = 10;
    private final int ROWS = 20;
    private final TetrominoType [][] board = new TetrominoType[ROWS][COLS];
    private TetrominoHandler current;
    private final TetrominoQueue queue;
    private double score = 0;
    private int totalLines = 0;
    private TetrominoType holdPiece = null;
    public long lockDelay = 500_000_000;
    private long lockStartTime = 0;
    private boolean isTouchingGround = false;
    private boolean holdUsed = false;
    private final GameRenderer renderer;
    private long lastUpdate = 0;
    private final long fallDelay = 500_000_000; // 0.5s

    public GameEngine(GameRenderer renderer) {
        this.renderer = renderer;
        queue = new TetrominoQueue();

        spawnBlock();
    }
    public void start() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (current == null) return;
                if (now - lastUpdate > fallDelay) {
                    update(now);
                    lastUpdate = now;
                }
                // Rendering game
                renderer.renderBackground();
                renderer.renderBoard(board);

                renderer.renderGhostPiece(current, getGhostY());
                renderer.renderCurrentPiece(current);

                renderer.renderNext(queue.getPreview());
                renderer.renderHold(holdPiece);

            }
        }
        .start();
    }
    private void update(long now) {
        if (canMove(current.x, current.y + 1)) {
            current.y++;
            isTouchingGround = false;
        } else {
            // Piece touch ground
            if (!isTouchingGround) {
                isTouchingGround = true;
                lockStartTime = now;
            }

            // hết delay → lock
            // 0.5s
            if (now - lockStartTime > lockDelay) {
                lockBlock();

                int cleared = clearLines();
                totalLines += cleared;

                spawnBlock();
                isTouchingGround = false;
            }
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
        holdUsed = false; // Reset hold state every piece dropped
        current = piece;
    }

    public void hold() {
        if (holdUsed) return;
        TetrominoType currentType = current.type;
        if (holdPiece == null) {
            holdPiece = currentType;
            spawnBlock();
        } else {
            TetrominoType temp = holdPiece;
            holdPiece = currentType;
            current = new TetrominoHandler(temp, 3, getSpawnYOffset(temp));
        }
        holdUsed = true;
    }
    private void resetLockDelay() {
        if (isTouchingGround) {
            lockStartTime = System.nanoTime();
        }
    }

    // Line clear system
    private int clearLines() {
        int linesCleared = 0;
        int y = ROWS - 1;

        while (y >= 0) {
            boolean full = true;

            for (int x = 0; x < COLS; x++) {
                if (board[y][x] == null) {
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
            board[0][x] = null;
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
        int from = current.rotationState;
        int to = (from + 1) % 4;

        int[][] newShape = current.type.shapes[to];
        int[][] kicks = RotationSRS.getKicks(current.type, from, to);

        for (int[] k : kicks) {
            int newX = current.x + k[0];
            int newY = current.y + k[1];

            if (canPlace(newShape, newX, newY)) {
                current.x = newX;
                current.y = newY;
                current.rotationState = to;
                return;
            }
        }
    }
    public void rotateCCW() {
        int from = current.rotationState;
        int to = (from + 3) % 4;

        int[][] newShape = current.type.shapes[to];
        int[][] kicks = RotationSRS.getKicks(current.type, from, to);

        for (int[] k : kicks) {
            int newX = current.x + k[0];
            int newY = current.y + k[1];

            if (canPlace(newShape, newX, newY)) {
                current.x = newX;
                current.y = newY;
                current.rotationState = to;
                return;
            }
        }
    }

    // Logic
    private boolean canMove(int newX, int newY) {
        return canPlace(current.getShape(), newX, newY);
    }

    private boolean canPlace(int[][] shape, int newX, int newY) {
        for (int[] p : shape) {
            int x = newX + p[0];
            int y = newY + p[1];

            if (x < 0 || x >= COLS || y >= ROWS) return false;
            if (y >= 0 && board[y][x] != null) return false;
        }
        return true;
    }
    // Ghost piece handler
    public int getGhostY() {
        int y = current.y;

        while (canMove(current.x, y + 1)) {
            y++;
        }

        return y;
    }

    private void lockBlock() {
        for (int[] p : current.getShape()) {
            int x = current.x + p[0];
            int y = current.y + p[1];
            if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                board[y][x] = current.type;
            }
        }
    }
}