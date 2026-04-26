package org.example.ascendrix;

import javafx.animation.AnimationTimer;

public class GameEngine {
    private final int COLS = 10;
    private final int ROWS = 20;
    private static final int SPAWN_X = 3;
    public InputHandler input;
    private final TetrominoType [][] board = new TetrominoType[ROWS][COLS];
    private TetrominoHandler current;
    private final TetrominoQueue queue;
    private final Ruleset ruleset;
    private TetrominoType holdPiece = null;
    public int totalLines = 0;
    private boolean holdUsed = false;
    private final GameRenderer renderer;
    private long frame = 0;
    private GameState state = GameState.RUNNING;

    public void setInput(InputHandler input) {
        this.input = input;
    }
    public GameEngine(GameRenderer renderer, Ruleset ruleset, DefaultHandling handling) {
        this.renderer = renderer;
        this.ruleset = ruleset;
        queue = new TetrominoQueue();

        spawnBlock();
    }

    public void start() {
        new AnimationTimer() {

            @Override
            public void handle(long now) {

                if (current == null) return;

                update();   //logic
                render();   //render
            }

        }.start();
    }
    private void render() {
        renderer.renderBackground();
        renderer.renderBoard(board);

        if (getState() != GameState.RUNNING) {
            renderer.renderBoardOverlay();
        }
        renderer.renderGhostPiece(current, getGhostY());
        renderer.renderCurrentPiece(current);

        renderer.renderNext(queue.getPreview());
        renderer.renderHold(holdPiece);
        renderer.renderGameplayUI();
    }
    public GameState getState() { return state; }
    public boolean isRunning() { return state == GameState.RUNNING; }

    public void end() {         // called by GameMode when goal is reached
        state = GameState.GAME_CLEARED;
    }
    public void topOut() {      // called when a piece can't spawn
        state = GameState.GAME_OVER;
    }

    private void update() {

        ruleset.handling.update(frame, input, this);
        ruleset.gravity.update(frame, this);
        ruleset.lockDelay.update(frame, this);
        frame++;
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
        TetrominoHandler piece = new TetrominoHandler(type, SPAWN_X, getSpawnYOffset(type));
        holdUsed = false;

        if (!canPlace(piece.getBlocks(), piece.x, piece.y)) {
            topOut();
            current = piece;
            return;
        }

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

    // Line clear system
    private int clearLines() {
        int linesCleared = 0;
        int writeY = ROWS - 1;

        for (int readY = ROWS - 1; readY >= 0; readY--) {
            boolean full = true;

            for (int x = 0; x < COLS; x++) {
                if (board[readY][x] == null) {
                    full = false;
                    break;
                }
            }

            if (full) {
                linesCleared++;
            } else {
                System.arraycopy(board[readY], 0, board[writeY], 0, COLS);
                writeY--;
            }
        }
        for (int y = 0; y < linesCleared; y++) {
            for (int x = 0; x < COLS; x++) {
                board[y][x] = null;
            }
        }

        return linesCleared;
    }
    // Remove filled line
    private void removeLine(int line) {
        for (int y = line; y > 0; y--) {
            System.arraycopy(board[y - 1], 0, board[y], 0, COLS);
        }

        // Clear upper line
        for (int x = 0; x < COLS; x++) {
            board[0][x] = null;
        }
    }
    // Movement
    public void move(int dir) {
        if (canMove(current.x + dir, current.y)) {
            current.x += dir;
            notifyMoveOrRotate();
        }
    }

    public void softDrop() {
        if (canMove(current.x, current.y + 1)) current.y++;
    }
    public void lock() {
        lockBlock();

        int cleared = clearLines();
        totalLines += cleared;
        if (canPlace(current.getBlocks(), current.x, current.y))
            spawnBlock();
    }
    public void hardDrop() {

        int y = current.y;
        while (canMove(current.x, y + 1)) y++;
        current.y = y;

        lockBlock();
        // Clear line after lock delay ends
        int cleared = clearLines();
        totalLines += cleared;

        spawnBlock();
    }

    public void notifyMoveOrRotate() {
        ruleset.lockDelay.onMoveOrRotate(frame);
    }

    public void rotateCW() {
        int prevRotation = current.rotation;
        ruleset.rotationSystem.tryRotate(current, 1, this);
        if (current.rotation != prevRotation) notifyMoveOrRotate();
    }

    public void rotateCCW() {
        int prevRotation = current.rotation;
        ruleset.rotationSystem.tryRotate(current, -1, this);
        if (current.rotation != prevRotation) notifyMoveOrRotate();
    }

    // Logic
    public boolean canMove(int newX, int newY) {
        return canPlace(current.getBlocks(), newX, newY);
    }
    public boolean canMoveHorizontal(int dir) {
        return canMove(current.x + dir, current.y);
    }

    public boolean canPlace(int[][] shape, int newX, int newY) {
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

        for (int[] p : current.getBlocks()) {
            int x = current.x + p[0];
            int y = current.y + p[1];
            if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                board[y][x] = current.type;
            }
        }
    }

    public void tryFall() {
        if (canMove(current.x, current.y + 1)) {
            current.y++;
        }
    }

    public boolean isOnGround() {
        return !canMove(current.x, current.y + 1);
    }
}

