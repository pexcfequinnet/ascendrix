package org.example.ascendrix.MainGame.Engine;
// Library
import javafx.animation.AnimationTimer;
import org.example.ascendrix.GameData.ScoreManager;
import org.example.ascendrix.GameMode.GameMode;
import org.example.ascendrix.GameMode.GameModeHandler;
import org.example.ascendrix.GameMode.Master.FadeMap;
import org.example.ascendrix.Input.*;
import org.example.ascendrix.MainGame.Renderer.GameRenderer;
import org.example.ascendrix.MainGame.Ruleset.RulesetHandler;
import org.example.ascendrix.Movement.*;
import org.example.ascendrix.Rotation.RotationDirection;
import org.example.ascendrix.Rotation.SRS.SRSSpinDetector;
import org.example.ascendrix.Rotation.SpinType;
import org.example.ascendrix.Tetromino.*;

import java.util.Arrays;

public class GameEngine {

    private AnimationTimer gameLoop = null;

    // Input Handling
    public InputHandler input;
    // Board handling
    public final int COLS = 10;
    public final int ROWS = 25;
    private static final int SPAWN_X = 3;
    public final TetrominoType[][] board = new TetrominoType[ROWS][COLS];
    // Tetromino + Bag queue handling
    private final boolean spawning = false;
    public TetrominoHandler current;
    private final TetrominoQueue queue;
    private TetrominoType holdPiece = null;
    // Hold slot + Line clear handling

    private TetrominoType pendingSpawnType = null;
    public int totalLines = 0;
    private boolean holdInProgress = false;
    private boolean holdUsed = false;
    private boolean waitingToSpawn = false;
    // Perfect clear Handling

    private long perfectClearDisplayTime = -1;
    private static final long PERFECT_CLEAR_DURATION = 750_000_000L; // 0.75s

    // Render
    private final GameRenderer renderer;
    private long lastUpdate = -1;
    // Game state handling
    public GameState state;
    public GameModeHandler modeHandler;
    private final GameMode mode;
    private final InputBuffer inputBuffer = new InputBuffer();
    public GamePhase phase;

    // Mode Handling: Define movement and game rules
    private final RulesetHandler rulesetHandler;
    public Handling handling;
    public SRSSpinDetector spinDetector = new SRSSpinDetector();
    public TetrominoQueue.DropType pendingDropType;
    public int pendingDropRows = 0;
    // Delay handling
    private boolean waitingForClearAnim = false;
    private long clearAnimStart = -1;
    private long clearAnimNs = 0;
    // Countdown timer
    public int countdown = 1;
    private long lastTick;
    public GameTimer timer = new GameTimer(); // Timer

    // End roll for needed game modes
    private FadeMap fadeMap; // Master Rolls
    private int[][] lastLockedBlocks;
    private int lastLockedX, lastLockedY;
    // Garbage map for needed game mode
    private final boolean[][] garbageMap = new boolean[ROWS][COLS];

    /* ============================== ALL FUNCTIONS ============================== */
    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    public void setInput(InputHandler input) {
        this.input = input;
    }


    public GameEngine(GameRenderer renderer, GameModeHandler modeHandler, GameMode mode) {
        this.mode = mode;
        this.modeHandler = modeHandler;
        this.rulesetHandler = modeHandler.getRuleset();
        this.handling = this.rulesetHandler.handling;
        queue = new TetrominoQueue();
        this.renderer = renderer;
    }
    public void start() {
        phase = GamePhase.COUNTDOWN;
        state = GameState.RUNNING; // Khởi tạo state ban đầu
        lastTick = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (state == GameState.PAUSED) {
                    lastUpdate = now;
                    lastTick = now;
                    return;
                }

                if (phase == GamePhase.COUNTDOWN) updateCountdown(now);
                if (phase == GamePhase.PLAYING) update(now);
                render(now);
            }
        };

        gameLoop.start();
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public void pause() {
        state = GameState.PAUSED;
        if (timer != null) timer.pause(); // Dừng đồng hồ đếm
    }

    public void resume() {
        state = GameState.RUNNING;
        if (timer != null) timer.resume(); // Chạy lại đồng hồ đếm
    }
    // Rendering function: Initialize the game
    private void render(long now) {
        renderer.renderBoard(board, modeHandler.getBoardContext(board), this);
        if (getPhase() == GamePhase.GAME_OVER)  renderer.renderGameOver();
        if (getPhase() == GamePhase.CLEARED)    renderer.renderGameComplete();

        if (!waitingToSpawn && !waitingForClearAnim && current != null) {
            renderer.renderCurrentPiece(current, this);
            renderer.renderGhostPiece(current, getGhostY(), this);
        }

        if (perfectClearDisplayTime != -1 && now - perfectClearDisplayTime < PERFECT_CLEAR_DURATION)
            renderer.renderPerfectClear();
        else
            perfectClearDisplayTime = -1;

        renderer.renderNext(queue.getPreview(), this);
        renderer.renderHold(holdPiece, this);
        renderer.renderHUD(modeHandler, timer, now);
        renderer.renderCountdown(phase, countdown);
    }
    public GamePhase getPhase() { return phase; }

    public void clearGame() {         // called by GameModeHandler when goal is reached
        phase = GamePhase.CLEARED;
        state = GameState.STOPPED;
        timer.pause();
    }

    public void topOut() {
        if (modeHandler.isRollActive()) {
            clearGame();
            return;
        }
        else
        {
            phase = GamePhase.GAME_OVER;
            state = GameState.STOPPED;
            timer.pause();
        }
    }

    private void update(long now) {
        if (lastUpdate == -1) lastUpdate = now;
        if (phase != GamePhase.PLAYING) return;

        fadeMap = modeHandler.getFadeMap();
        modeHandler.update(now, this); // moved after phase check

        if (fadeMap != null) {
            fadeMap.updateBoardFade(now, board);
            if (fadeMap.isFadingBoard()) return;
        }

        if (waitingForClearAnim) {
            handleClearAnim(now);
            return;
        }

        handleSpawnDelay(now, fadeMap);

        if (!waitingToSpawn && current != null) {
            rulesetHandler.handling.update(now, input, this);
            rulesetHandler.gravity.update(now, this);
            rulesetHandler.lockDelay.update(now, this);
        }
    }
    /* ========================== GAME LOGIC  ========================== */
    // Vertical piece offset
    private int getSpawnYOffset(TetrominoType type) {
        if(type == TetrominoType.I)
            return 4;
        return 5;
    }

    public boolean isSpawning() {
        return spawning || waitingToSpawn;
    }

    private void spawnBlock() {
        TetrominoType type = pendingSpawnType != null ? pendingSpawnType : queue.next();
        pendingSpawnType = null;
        holdUsed = false;
        // IHS
        if (!holdInProgress && modeHandler.supportsIHS() && inputBuffer.isHoldHeld()) {
            holdInProgress = true;
            if (holdPiece == null) {
                holdPiece = type;
                pendingSpawnType = null;
                holdInProgress = false;
                holdUsed = true;
                spawnBlock(); // spawn next from queue
                return;
            } else {
                TetrominoType temp = holdPiece;
                holdPiece = type;
                type = temp; // swap type, continue spawning with hold piece
            }
            holdUsed = true;
            holdInProgress = false;
        }

        current = new TetrominoHandler(type, SPAWN_X, getSpawnYOffset(type));

        // IRS
        if (modeHandler.supportsIRS()) {

            RotationDirection irs =
                    input.getHeldIRS();

            if (irs != null) {
                rotate(irs);
            }
        }
        // IMS

        int dir = inputBuffer.getBufferedDirection();
        if (dir != 0) {
            move(dir);
            inputBuffer.clearDirection();
        }
        if (!canPlace(current.getBlocks(), current.x, current.y)) {
            topOut();
            gameOver();
            return;
        }
        MovementSystem movement =
                (MovementSystem) rulesetHandler.handling;
        if (movement.isDasCharged(System.nanoTime())) {
            int dasDir = movement.getDirection();
            if (movement.config.instantArr) {
                while (canMove(current.x + dasDir, current.y)) {
                    move(dasDir);
                }
            } else {

                move(dasDir);
            }
        }
        current.movedBeforeRotation = true;
        current.movedSinceLastRotation = true;
    }
    public void hold() {
        if (waitingToSpawn || waitingForClearAnim) return;
        if (holdUsed) return;
        if (holdInProgress) return;

        holdInProgress = true;
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
        holdInProgress = false;
    }

    // Line clear system
    private int clearLines(FadeMap fadeMap) {
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
                System.arraycopy(garbageMap[readY], 0, garbageMap[writeY], 0, COLS);

                if (fadeMap != null) {
                    fadeMap.copyRow(readY, writeY);
                }

                writeY--;
            }
        }

        // clear top rows
        for (int y = 0; y <= writeY; y++) {
            Arrays.fill(board[y], null);
            Arrays.fill(garbageMap[y], false);
        }

        if (fadeMap != null && linesCleared > 0) {
            fadeMap.clearTopRows(linesCleared);
        }

        return linesCleared;
    }
    // Movement
    public void move(int dir) {
        if (waitingToSpawn || waitingForClearAnim) return;
        if (fadeMap != null && fadeMap.isFadingBoard()) return;
        if (canMove(current.x + dir, current.y)) {
            current.x += dir;

            current.movedAfterRotation = true;
            current.lastMoveWasRotation = false;
            current.movedSinceLastRotation = true;
            notifyMoveOrRotate(System.nanoTime());
        }
    }

    public void softDrop(long now) {
        if (waitingToSpawn) return;
        if (fadeMap != null && fadeMap.isFadingBoard()) return;
        if (canMove(current.x, current.y + 1)) {
            current.y++;
            current.droppedByPlayer = true;
            pendingDropRows++;
            pendingDropType = TetrominoQueue.DropType.SOFT;
            if (current.y > current.yAtRotation)
                current.movedAfterRotation = true;
            notifyMoveOrRotate(now);
        }
    }

    public void hardDrop(long now) {
        if (waitingToSpawn || spawning) return;
        if (fadeMap != null && fadeMap.isFadingBoard()) return;
        int rows = 0;
        int y = current.y;
        while (canMove(current.x, y + 1)) { y++; rows++; }
        if (rows > 0) current.droppedByPlayer = true;
        current.y = y;
        if (rows > 0) { pendingDropRows += rows; pendingDropType = TetrominoQueue.DropType.HARD; }
        if (current.y > current.yAtRotation) current.movedAfterRotation = true;
        notifyMoveOrRotate(now);
        lockBlock(now, modeHandler.getFadeMap());
    }
    public void lockBlock(long now) {
        lockBlock(now, fadeMap);
    }

    public void lockBlock(long now, FadeMap fadeMap) {
        SpinType spin = spinDetector.detect(current, this);
        lastLockedBlocks = current.getBlocks();
        lastLockedX = current.x;
        lastLockedY = current.y;
        for (int[] p : current.getBlocks()) {
            int x = current.x + p[0];
            int y = current.y + p[1];
            if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                board[y][x] = modeHandler.isDecolorActive()
                        ? TetrominoType.BONE
                        : current.type;
                garbageMap[y][x] = false;
            }
        }

        int cleared = clearLines(fadeMap);
        boolean perfectClear = modeHandler.supportsPerfectClear() && cleared > 0 && isPerfectClear();

        modeHandler.onPiecePlaced(this);

        if (perfectClear) {
            triggerPerfectClear(now);
        }

        totalLines += cleared;
        modeHandler.setPerfectClearFlag(perfectClear);
        modeHandler.onLinesCleared(cleared, spin, pendingDropRows, pendingDropType, this);

        pendingDropRows = 0;
        pendingDropType = TetrominoQueue.DropType.NONE;

        if (cleared > 0) {
            triggerLineClearAnim(rulesetHandler.are.getClearAnimNs(), now);
        } else {
            waitingToSpawn = true;
            rulesetHandler.are.trigger(false, now);
        }
    }

    public void triggerPerfectClear(long now) {
        perfectClearDisplayTime = now;
    }
    public void triggerLineClearAnim(long ns, long now) {
        waitingForClearAnim = true;
        clearAnimStart = now;
        clearAnimNs = ns;
    }

    private void handleClearAnim(long now) {
        if (!waitingForClearAnim) return;
        if (now - clearAnimStart >= clearAnimNs) {
            waitingForClearAnim = false;
            waitingToSpawn = true;
            rulesetHandler.are.trigger(true, now);
        }
    }

    // In update loop
    private void handleSpawnDelay(long now, FadeMap fadeMap) {
        if (!waitingToSpawn) return;
        if (fadeMap != null && fadeMap.isFadingBoard()) return;
        if (rulesetHandler.are.isDone(now)) {
            waitingToSpawn = false;
            rulesetHandler.are.reset();
            ((MovementSystem) rulesetHandler.handling).resetDAS();
            spawnBlock();
            modeHandler.onPieceSpawned(this);
        }
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
    public void rotate(RotationDirection dir) {
        switch (dir) {
            case CW -> rotateCW();
            case CCW -> rotateCCW();
        }
    }
    public void rotateCW() {
        if (waitingToSpawn || waitingForClearAnim) return;
        if (fadeMap != null && fadeMap.isFadingBoard()) return;


        boolean rotated =
                rulesetHandler.rotationSystem.tryRotate(
                        current,
                        RotationDirection.CW,
                        this
                );

        if (rotated) {
            notifyMoveOrRotate(System.nanoTime());

            current.lastMoveWasRotation = true;
            current.yAtRotation = current.y;
        }
    }

    public void rotateCCW() {
        if (waitingToSpawn || waitingForClearAnim) return;
        if (fadeMap != null && fadeMap.isFadingBoard()) return;

        boolean rotated =
                rulesetHandler.rotationSystem.tryRotate(
                        current,
                        RotationDirection.CCW,
                        this
                );
        if (rotated) {
            notifyMoveOrRotate(System.nanoTime());

            current.lastMoveWasRotation = true;
            current.yAtRotation = current.y;
        }
    }

    public void gameOver() {
        phase = GamePhase.GAME_OVER;
    }
    // Save score

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
    public void updateCountdown(long now) {
        if (now - lastTick >= 1_000_000_000L) {
            countdown--;
            lastTick = now;

            if (countdown < 0) {
                phase = GamePhase.PLAYING;
                timer.reset();
                timer.start();
                waitingToSpawn = true;
                rulesetHandler.are.trigger(false, now);
            }
        }
    }


    public int[][] getLastLockedBlocks() { return lastLockedBlocks; }
    public int getLastLockedX()          { return lastLockedX; }
    public int getLastLockedY()          { return lastLockedY; }

    public void debugSetLevel(int level) {
        modeHandler.debugSetLevel(level);
    }

    // Garbage for Overdrive
    public int getCols() { return COLS; }
    public int getRows() { return ROWS; }

    public void setGarbageCell(int row, int col) {
        board[row][col] = TetrominoType.GARBAGE;
        garbageMap[row][col] = true;
    }

    public boolean isGarbageCell(int row, int col) {
        return garbageMap[row][col];
    }

    public boolean pushBoardUp() {
        // check if top row has anything
        for (int col = 0; col < COLS; col++) {
            if (board[0][col] != null || garbageMap[0][col]) {
                topOut();
                return false;
            }
        }

        // shift entire board up
        for (int row = 0; row < ROWS - 1; row++) {
            System.arraycopy(board[row + 1], 0, board[row], 0, COLS);
            System.arraycopy(garbageMap[row + 1], 0, garbageMap[row], 0, COLS);
        }
        // clear bottom row
        Arrays.fill(board[ROWS - 1], null);
        Arrays.fill(garbageMap[ROWS - 1], false);
        pushCurrentPieceUp();
        return true;
    }
    public void pushCurrentPieceUp() {
        if (current != null) {
            current.y--;
        }
    }
    public boolean isColumnEmpty(int col) {
        for (int row = 0; row < ROWS; row++) {
            if (board[row][col] != null || garbageMap[row][col]) return false;
        }
        return true;
    }

    public boolean isGameOver() {
        return phase == GamePhase.GAME_OVER;
    }

    public boolean isCleared() {
        return phase ==  GamePhase.CLEARED;
    }


    public boolean isCellEmpty(int row, int col) {
        return board[row][col] == null && !garbageMap[row][col];
    }
}