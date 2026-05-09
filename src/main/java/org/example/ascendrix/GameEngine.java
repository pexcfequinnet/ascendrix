package org.example.ascendrix;
// Library
import javafx.animation.AnimationTimer;

public class GameEngine {
    // Input Handling
    public InputHandler input;
    // Board handling
    public final int COLS = 10;
    public final int ROWS = 25;
    private static final int SPAWN_X = 3;
    public final TetrominoType [][] board = new TetrominoType[ROWS][COLS];
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
    private final HUDHelper hud = new HUDHelper(); // for HUD stats, such as time or level
    //private long tick = 0;

    // Game state handling
    public GameState state;
    public GameModeHandler mode_handler;
    private final InputBuffer inputBuffer = new InputBuffer();
    public GamePhase phase;

    // Mode Handling: Define movement and game rules
    private final RulesetHandler rulesetHandler;
    public Handling handling;
    public SRSSpinDetector spinDetector = new SRSSpinDetector();
    public DropType pendingDropType;
    public int pendingDropRows = 0;

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


    public GameEngine(GameRenderer renderer, GameModeHandler modeHandler) {
        this.mode_handler = modeHandler;
        this.rulesetHandler = modeHandler.getRuleset();
        this.handling = this.rulesetHandler.handling;

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
        long now = System.nanoTime();
        renderer.renderBoard(board);
        if (getPhase() == GamePhase.GAME_OVER)
            renderer.renderGameOver();
        if (getPhase() == GamePhase.CLEARED)
            renderer.renderGameComplete();

        renderer.renderGhostPiece(current, getGhostY());
        renderer.renderCurrentPiece(current);
        renderer.renderNext(queue.getPreview());
        renderer.renderHold(holdPiece);
        renderer.renderHUD(mode_handler, timer, now);
        renderer.renderCountdown(phase, countdown);

    }
    public GamePhase getPhase() { return phase; }
    public boolean isRunning() { return state == GameState.RUNNING; }

    public void end() {         // called by GameModeHandler when goal is reached
        phase = GamePhase.CLEARED;
        state = GameState.STOPPED;
        timer.pause();
    }
    public void topOut() {      // called when a piece can't spawn
        phase = GamePhase.GAME_OVER;
        state = GameState.STOPPED;
        timer.pause();
    }

    private void update(long now) {
        if (lastUpdate == -1) {
            lastUpdate = now;
        }

//        long delta = now - lastUpdate;
//        lastUpdate = now;
//
//        if (delta > 100_000_000) {
//            delta = 100_000_000;
//        }


        //tick++;

        rulesetHandler.handling.update(now, input, this);
        rulesetHandler.gravity.update(now, this);
        rulesetHandler.lockDelay.update(now, this);
    }
    /* ========================== GAME LOGIC  ========================== */
    // Vertical piece offset
    private int getSpawnYOffset(TetrominoType type) {
        if(type == TetrominoType.I)
            return 4;
        return 5;
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
        piece.movedBeforeRotation = true;
        piece.movedSinceLastRotation = true;
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

            current.movedAfterRotation = true;
            current.lastMoveWasRotation = false;
            current.movedSinceLastRotation = true;
            notifyMoveOrRotate(System.nanoTime());
        }
    }

    public void softDrop(long now) {
        if (canMove(current.x, current.y + 1)) {
            current.y++;
            current.droppedByPlayer = true;
            pendingDropRows++;
            pendingDropType = DropType.SOFT;
            if (current.y > current.yAtRotation)
                current.movedAfterRotation = true;
            notifyMoveOrRotate(now);
        }
    }

    public void hardDrop(long now) {
        int rows = 0;
        int y = current.y;
        while (canMove(current.x, y + 1)) {
            y++;
            rows++;
        }
        if (rows > 0) current.droppedByPlayer = true;
        current.y = y;

        if (rows > 0) {
            pendingDropRows += rows;
            pendingDropType = DropType.HARD;
        }

        if (current.y > current.yAtRotation){
            current.movedAfterRotation = true;
        }



            // Important: notify lock system
        notifyMoveOrRotate(now);
        lockBlock();
    }

    public void lockBlock() {
        SpinType spin = spinDetector.detect(current, this);
        for (int[] p : current.getBlocks()) {
            int x = current.x + p[0];
            int y = current.y + p[1];
            if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                board[y][x] = current.type;
            }
        }

        int cleared = clearLines();
        if (mode_handler.supportsPerfectClear() && cleared > 0 && isPerfectClear()) {
            System.out.println("how tf");
            renderer.renderPerfectClear();
        }
        totalLines += cleared;
        if (mode_handler.supportsPerfectClear()) {
            mode_handler.setPerfectClearFlag(cleared > 0 && isPerfectClear());
        }
        mode_handler.onLinesCleared(cleared, spin, pendingDropRows, pendingDropType, this); // was deleted
        // Reset dropBonus for Marathon
        pendingDropRows = 0;
        pendingDropType = DropType.NONE;


        spawnBlock();
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
        current.lastMoveWasRotation = true;
        current.yAtRotation = current.y;
    }

    public void rotateCCW() {
        int prevRotation = current.rotation;
        rulesetHandler.rotationSystem.tryRotate(current, -1, this);
        if (current.rotation != prevRotation) notifyMoveOrRotate(System.nanoTime());
        current.lastMoveWasRotation = true;
        current.yAtRotation = current.y;
    }

    // Logic
    public boolean canMove(int newX, int newY) {
        return canPlace(current.getBlocks(), newX, newY);
    }

    public boolean canMoveHorizontal(int dir) {
        return canMove(current.x + dir, current.y);
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

    public boolean isPerfectClear() {
        for (int y = 0; y < ROWS; y++)
            for (int x = 0; x < COLS; x++)
                if (board[y][x] != null) return false;
        return true;
    }
    // Countdown + timer
    public void updateCountdown() {
        long now = System.currentTimeMillis();

        if (now - lastTick >= 1000) {
            countdown--;
            lastTick = now;

            if (countdown < 0) {
                phase = GamePhase.PLAYING;
                timer.reset();
                timer.start();
            }
        }
    }
}

