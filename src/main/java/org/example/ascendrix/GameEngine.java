package org.example.ascendrix;

import javafx.animation.AnimationTimer;

public class GameEngine {
    // Input Handling
    public InputHandler input;
    // Board handling
    private final int COLS = 10;
    private final int ROWS = 20;
    private static final int SPAWN_X = 3;
    private final TetrominoType [][] board = new TetrominoType[ROWS][COLS];
    // Tetromino + Bag queue handling
    private boolean spawning = false;
    private TetrominoHandler current;
    private final TetrominoQueue queue;
    private TetrominoType holdPiece = null;
    // Hold slot + Line clear handling
    public int totalLines = 0;
    private boolean holdUsed = false;
    // Render
    private final GameRenderer renderer;
    private long lastUpdate = -1;
    private long tick = 0;

    // Game state handling
    public GameState state;
    public GameModeHandler mode_handler;
    public GameRuntimeState runtime_state;
    private final InputBuffer inputBuffer = new InputBuffer();
    public GamePhase phase;

    // Mode Handling: Define movement and game rules
    private final RulesetHandler rulesetHandler;
    public Handling handling;
    // Countdown timer
    public int countdown = 1;
    private long lastTick;
    public GameTimer timer = new GameTimer(); // Timer
    /* ============================== ALL FUNCTIONS ============================== */
    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    public void setInput(InputHandler input) {
        this.input = input;
    }
    public GameEngine(GameRenderer renderer, RulesetHandler rulesetHandler, GameModeHandler modeHandler) {
        this.rulesetHandler = rulesetHandler;
        this.handling = rulesetHandler.handling;
        this.mode_handler = modeHandler;

        queue = new TetrominoQueue();
        spawnBlock();

        this.renderer = renderer;
    }

    public void start() {
        phase = GamePhase.COUNTDOWN; // ← initialize phase
        lastTick = System.currentTimeMillis();

        new AnimationTimer() {
            public void handle(long now) {
                if (phase == GamePhase.COUNTDOWN) {
                    updateCountdown();
                }
                if (phase == GamePhase.PLAYING) {
                    update(now);
                }
                render();
            }
        }.start();
    }

    // Rendering function: Initialize the game
    private void render() {
        renderer.renderBackground();
        renderer.renderBoard(board);
        if (getState() == GameState.GAME_OVER) {
            renderer.renderBoardOverlay();
        }
        renderer.renderGhostPiece(current, getGhostY());
        renderer.renderCurrentPiece(current);

        renderer.renderNext(queue.getPreview());
        renderer.renderHold(holdPiece);

        renderer.renderHUD(mode_handler,timer);
        renderer.renderCountdown(phase, countdown);
    }
    public GameState getState() { return state; }
    public boolean isRunning() { return state == GameState.RUNNING; }

    public void end() {         // called by GameModeHandler when goal is reached
        state = GameState.GAME_CLEARED;
        timer.pause();
    }
    public void topOut() {      // called when a piece can't spawn
        state = GameState.GAME_OVER;
        timer.pause();
    }

    private void update(long now) {
        if (lastUpdate == -1) {
            lastUpdate = now;
        }

        long delta = now - lastUpdate;
        lastUpdate = now;

        if (delta > 100_000_000) {
            delta = 100_000_000;
        }
        // Time-based render system
        rulesetHandler.handling.update(now, input, this);
        rulesetHandler.gravity.update(now, this);
        rulesetHandler.lockDelay.update(now, this);
        tick++;
    }
    /* ========================== GAME LOGIC  ========================== */
    // Vertical piece offset
    private int getSpawnYOffset(TetrominoType type) {
        if(type == TetrominoType.I)
            return -1;
        return 0;
    }
    public boolean isSpawning() {
        return spawning;
    }
    private void spawnBlock() {
        TetrominoType type = queue.next();
        TetrominoHandler piece = new TetrominoHandler(type, SPAWN_X, getSpawnYOffset(type));
        holdUsed = false;

        if (!canPlace(piece.getBlocks(), piece.x, piece.y)) {
            current = piece;
            topOut();
            return;
        }
        spawning = true;
        current = piece;
        spawning = false;
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

    // Movement
    public void move(int dir) {
        if (canMove(current.x + dir, current.y)) {
            current.x += dir;
            notifyMoveOrRotate(System.nanoTime());
        }
    }

    public void softDrop(long now) {
        if (canMove(current.x, current.y + 1)) {
            current.y++;
            notifyMoveOrRotate(now);
        }
    }
    public void lock() {
        lockBlock();

        int cleared = clearLines();
        totalLines += cleared;
    }
    public void hardDrop(long now) {

        int y = current.y;

        while (canMove(current.x, y + 1)) {
            y++;
        }

        current.y = y;

        // Important: notify lock system
        notifyMoveOrRotate(now);

        lockBlock();

        int cleared = clearLines();
        totalLines += cleared;
    }
    private void lockBlock() {

        for (int[] p : current.getBlocks()) {
            int x = current.x + p[0];
            int y = current.y + p[1];
            if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                board[y][x] = current.type;
            }
        }
        int cleared = clearLines();

        if (cleared > 0) {
            mode_handler.onLinesCleared(cleared, this);
        }
        spawnBlock();
    }

    public boolean tryFall() {
        if (canMove(current.x, current.y + 1)) {
            current.y++;
            return true;
        }
        return false;
    }

    public boolean isOnGround() {
        return !canMove(current.x, current.y + 1);
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
    public void notifyMoveOrRotate(long now) {
        rulesetHandler.lockDelay.onMoveOrRotate(now);
    }

    public void rotateCW() {
        int prevRotation = current.rotation;
        rulesetHandler.rotationSystem.tryRotate(current, 1, this);
        if (current.rotation != prevRotation) notifyMoveOrRotate(System.nanoTime());
    }

    public void rotateCCW() {
        int prevRotation = current.rotation;
        rulesetHandler.rotationSystem.tryRotate(current, -1, this);
        if (current.rotation != prevRotation) notifyMoveOrRotate(System.nanoTime());
    }

    // Logic
    public boolean canMove(int newX, int newY) {
        return canPlace(current.getBlocks(), newX, newY);
    }
    public boolean canMoveHorizontal(int dir) {
        return canMove(current.x + dir, current.y);
    }

    // Countdown + timer
    public void updateCountdown() {
        long now = System.currentTimeMillis();

        if (now - lastTick >= 1000) {
            countdown--;
            lastTick = now;

            if (countdown < 0) {
                phase = GamePhase.PLAYING; // ← this is what unblocks update()
                timer.reset();
                timer.start();
            }
        }
    }
}

