package org.example.ascendrix.model.state;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.board.FadeMap;
import org.example.ascendrix.model.input.InputBuffer;
import org.example.ascendrix.model.mechanics.ARETimer;
import org.example.ascendrix.model.mechanics.MovementProcessor;
import org.example.ascendrix.model.mechanics.rotation.RotationDirection;
import org.example.ascendrix.model.mechanics.rotation.SRSSpinDetector;
import org.example.ascendrix.model.mechanics.rotation.SpinType;
import org.example.ascendrix.model.mode.GameMode;
import org.example.ascendrix.model.mode.ModeRuleset;
import org.example.ascendrix.model.piece.ActivePiece;
import org.example.ascendrix.model.piece.HoldSlot;
import org.example.ascendrix.model.piece.TetrominoQueue;
import org.example.ascendrix.model.piece.TetrominoType;

public class GameSession {
    private static final int SPAWN_X = 3;
    private static final long PERFECT_CLEAR_DURATION_NS = 750_000_000L;

    private final Board board = new Board();
    private final TetrominoQueue queue = new TetrominoQueue();
    private final HoldSlot holdSlot = new HoldSlot();
    private final GameTimer timer = new GameTimer();
    private final SRSSpinDetector spinDetector = new SRSSpinDetector();

    private final GameMode mode;
    private final ModeRuleset ruleset;
    private final InputBuffer inputBuffer;

    private ActivePiece current;
    private TetrominoType pendingSpawnType = null;
    private TetrominoQueue.DropType pendingDropType = TetrominoQueue.DropType.NONE;
    private int pendingDropRows = 0;
    private int totalLines = 0;

    private int[][] lastLockedBlocks;
    private int lastLockedX;
    private int lastLockedY;

    private GamePhase phase = GamePhase.COUNTDOWN;
    private GamePhase phaseBeforePause = GamePhase.COUNTDOWN;
    private int countdown = 1;
    private long lastTick = -1L;

    private boolean waitingToSpawn = false;
    private boolean waitingForClearAnim = false;
    private long clearAnimStart = -1L;
    private long clearAnimNs = 0L;
    private long perfectClearDisplayTime = -1L;

    public GameSession(GameMode mode, ModeRuleset ruleset, InputBuffer inputBuffer) {
        this.mode = mode;
        this.ruleset = ruleset;
        this.inputBuffer = inputBuffer;
    }

    /* ========================== LIFECYCLE ========================== */

    public void start(long now) {
        phase = GamePhase.COUNTDOWN;
        lastTick = now;
    }

    public void pause() {
        if (phase == GamePhase.PLAYING || phase == GamePhase.COUNTDOWN || phase == GamePhase.ROLL) {
            phaseBeforePause = phase;
            phase = GamePhase.PAUSED;
            timer.pause();
        }
    }

    public void resume(long now) {
        if (phase == GamePhase.PAUSED) {
            phase = phaseBeforePause;
            lastTick = now;
            if (phase == GamePhase.PLAYING || phase == GamePhase.ROLL) {
                timer.resume(now);
            }
        }
    }

    public void update(long now) {
        update(now, 0, false);
    }

    public void update(long now, int horizontalDir, boolean softDropHeld) {
        if (phase == GamePhase.COUNTDOWN) {
            updateCountdown(now);
            return;
        }
        if (phase != GamePhase.PLAYING && phase != GamePhase.ROLL) {
            return;
        }

        lastTick = now;

        FadeMap fadeMap = ruleset.getFadeMap();
        board.setFadeMap(fadeMap);
        ruleset.update(now, this);

        board.updateFade(now);
        if (board.isFading()) {
            return;
        }

        if (waitingForClearAnim) {
            handleClearAnim(now);
            return;
        }

        handleSpawnDelay(now);

        if (!waitingToSpawn && current != null) {
            ruleset.getHandling().update(now, horizontalDir, softDropHeld, this);
            ruleset.getGravity().update(now, this);
            ruleset.getLockDelay().update(now, this);
        }
    }

    public void clearGame() {
        phase = GamePhase.CLEARED;
        timer.pause();
    }

    public void topOut() {
        if (ruleset.isRollActive()) {
            clearGame();
        } else {
            phase = GamePhase.GAME_OVER;
            timer.pause();
        }
    }

    public void gameOver() {
        phase = GamePhase.GAME_OVER;
        timer.pause();
    }

    /* ========================== SPAWN & HOLD ========================== */

    private int getSpawnYOffset(TetrominoType type) {
        return (type == TetrominoType.I) ? 4 : 5;
    }

    private void spawnBlock(long now) {
        TetrominoType type = (pendingSpawnType != null) ? pendingSpawnType : queue.next();
        pendingSpawnType = null;
        holdSlot.resetTurn();

        // IHS (Initial Hold System)
        if (ruleset.supportsIHS() && holdSlot.canHold() && inputBuffer.isHoldHeld()) {
            if (holdSlot.isEmpty()) {
                holdSlot.swap(type);
                spawnBlock(now);
                return;
            } else {
                type = holdSlot.swap(type);
            }
        }

        current = new ActivePiece(type, SPAWN_X, getSpawnYOffset(type));

        // IRS (Initial Rotation System)
        if (ruleset.supportsIRS()) {
            RotationDirection irs = inputBuffer.getHeldIRS();
            if (irs != null) {
                rotate(irs, now);
            }
        }

        // IMS (Initial Movement System)
        int dir = inputBuffer.getBufferedDirection();
        if (dir != 0) {
            move(dir, now);
            inputBuffer.clearDirection();
        }

        if (!board.canPlace(current.getBlocks(), current.getX(), current.getY())) {
            topOut();
            return;
        }

        MovementProcessor movement = ruleset.getHandling();
        if (movement.isDasCharged(now)) {
            int dasDir = movement.getDirection();
            if (movement.getConfig().isInstantArr()) {
                while (canMove(current.getX() + dasDir, current.getY())) {
                    move(dasDir, now);
                }
            } else {
                move(dasDir, now);
            }
        }

        current.markSpawnMovementComplete();
    }

    public void hold() {
        hold(currentNow());
    }

    public void hold(long now) {
        if (waitingToSpawn || waitingForClearAnim || !holdSlot.canHold() || current == null) {
            return;
        }

        TetrominoType currentType = current.getType();
        if (holdSlot.isEmpty()) {
            holdSlot.swap(currentType);
            spawnBlock(now);
        } else {
            TetrominoType swapped = holdSlot.swap(currentType);
            current = new ActivePiece(swapped, SPAWN_X, getSpawnYOffset(swapped));

            if (!board.canPlace(current.getBlocks(), current.getX(), current.getY())) {
                topOut();
                return;
            }
            current.markSpawnMovementComplete();
        }
    }
    /* ========================== MOVEMENT & ROTATION ========================== */

    public void move(int dir) {
        move(dir, currentNow());
    }

    public void move(int dir, long now) {
        if (waitingToSpawn || waitingForClearAnim || board.isFading() || current == null) return;
        if (canMove(current.getX() + dir, current.getY())) {
            current.moveHorizontal(dir);
            notifyMoveOrRotate(now);
        }
    }

    public void softDrop(long now) {
        if (waitingToSpawn || board.isFading() || current == null) return;
        if (canMove(current.getX(), current.getY() + 1)) {
            current.moveDown(true);
            pendingDropRows++;
            pendingDropType = TetrominoQueue.DropType.SOFT;
            notifyMoveOrRotate(now);
        }
    }

    public void hardDrop() {
        hardDrop(currentNow());
    }

    public void hardDrop(long now) {
        if (waitingToSpawn || board.isFading() || current == null) return;
        int rows = 0;
        while (canMove(current.getX(), current.getY() + 1)) {
            current.moveDown(true);
            rows++;
        }
        if (rows > 0) {
            pendingDropRows += rows;
            pendingDropType = TetrominoQueue.DropType.HARD;
        }
        notifyMoveOrRotate(now);
        lockBlock(now, ruleset.getFadeMap());
    }

    public void rotateCW() {
        rotate(RotationDirection.CW, currentNow());
    }

    public void rotateCCW() {
        rotate(RotationDirection.CCW, currentNow());
    }

    public void rotate(RotationDirection dir, long now) {
        if (waitingToSpawn || waitingForClearAnim || board.isFading() || current == null) return;

        boolean rotated = ruleset.getRotationSystem().tryRotate(current, dir, board);
        if (rotated) {
            notifyMoveOrRotate(now);
            current.onRotated();
        }
    }

    public void notifyMoveOrRotate(long now) {
        ruleset.getLockDelay().onMoveOrRotate(now);
    }

    /* ========================== LOCKING & DELAYS ========================== */

    public void lockBlock(long now) {
        lockBlock(now, board.getFadeMap());
    }

    public void lockBlock(long now, FadeMap fadeMap) {
        if (current == null) return;

        SpinType spin = spinDetector.detect(current, board);
        lastLockedBlocks = current.getBlocks();
        lastLockedX = current.getX();
        lastLockedY = current.getY();

        board.lockBlocks(
                lastLockedBlocks,
                lastLockedX,
                lastLockedY,
                current.getType(),
                ruleset.isDecolorActive()
        );

        int cleared = board.clearFullLines();
        boolean perfectClear = ruleset.supportsPerfectClear() && cleared > 0 && board.isEmpty();

        ruleset.onPiecePlaced(this);
        if (perfectClear) {
            triggerPerfectClear(now);
        }

        totalLines += cleared;
        ruleset.setPerfectClearFlag(perfectClear);
        ruleset.onLinesCleared(cleared, spin, pendingDropRows, pendingDropType, this);

        pendingDropRows = 0;
        pendingDropType = TetrominoQueue.DropType.NONE;
        current = null;

        if (cleared > 0) {
            triggerLineClearAnim(ruleset.getARE().getClearAnimNs(), now);
        } else {
            waitingToSpawn = true;
            ruleset.getARE().trigger(false, now);
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
            ruleset.getARE().trigger(true, now);
        }
    }

    private void handleSpawnDelay(long now) {
        if (!waitingToSpawn || board.isFading()) return;
        ARETimer are = ruleset.getARE();
        if (are.isDone(now)) {
            waitingToSpawn = false;
            are.reset();
            ruleset.getHandling().resetDAS();
            spawnBlock(now);
            ruleset.onPieceSpawned(this);
        }
    }

    private void updateCountdown(long now) {
        if (now - lastTick >= 1_000_000_000L) {
            countdown--;
            lastTick = now;
        }
        if (countdown < 0) {
            phase = GamePhase.PLAYING;
            timer.reset();
            timer.start(now);
            waitingToSpawn = true;
            ruleset.getARE().trigger(false, now);
        }
    }

    private long currentNow() {
        return (lastTick > 0L) ? lastTick : System.nanoTime();
    }

    /* ========================== BOARD & PIECE QUERIES ========================== */

    public boolean canMove(int newX, int newY) {
        return current != null && board.canPlace(current.getBlocks(), newX, newY);
    }

    public boolean canMoveHorizontal(int dir) {
        return current != null && canMove(current.getX() + dir, current.getY());
    }

    public boolean tryFall() {
        if (current != null && canMove(current.getX(), current.getY() + 1)) {
            current.moveDown(false);
            return true;
        }
        return false;
    }

    public boolean isOnGround() {
        return current != null && !canMove(current.getX(), current.getY() + 1);
    }

    public int getGhostY() {
        if (current == null) return 0;
        int y = current.getY();
        while (canMove(current.getX(), y + 1)) {
            y++;
        }
        return y;
    }

    public boolean pushBoardUp() {
        if (!board.pushUp()) {
            topOut();
            return false;
        }
        if (current != null) {
            current.pushUp();
        }
        return true;
    }

    public boolean isPerfectClearVisible(long now) {
        if (perfectClearDisplayTime == -1L) return false;
        if (now - perfectClearDisplayTime < PERFECT_CLEAR_DURATION_NS) return true;
        perfectClearDisplayTime = -1L;
        return false;
    }

    public void debugSetLevel(int level) {
        ruleset.debugSetLevel(level);
    }

    public record GameResult(String displayText, Object rawValue, long elapsedMs) {
        public GameResult(String displayText, Object rawValue) {
            this(displayText, rawValue, 0L);
        }

        public long asSortValue() {
            return (rawValue instanceof Number n) ? n.longValue() : 0L;
        }
    }

    /* ========================== GETTERS ========================== */

    public Board getBoard()                     { return board; }
    public ActivePiece getCurrentPiece()        { return current; }
    public TetrominoQueue getQueue()            { return queue; }
    public TetrominoType getHoldPiece()         { return holdSlot.getHeldType(); }
    public GameMode getMode()                   { return mode; }
    public ModeRuleset getRuleset()             { return ruleset; }
    public InputBuffer getInputBuffer()         { return inputBuffer; }
    public GameTimer getTimer()                 { return timer; }
    public GamePhase getPhase()                 { return phase; }
    public int getCountdown()                   { return countdown; }
    public int getTotalLines()                  { return totalLines; }
    public boolean isSpawning()                 { return waitingToSpawn; }
    public boolean isWaitingForClearAnim()      { return waitingForClearAnim; }
    public boolean isPaused()                   { return phase == GamePhase.PAUSED; }
    public boolean isGameOver()                 { return phase == GamePhase.GAME_OVER; }
    public boolean isCleared()                  { return phase == GamePhase.CLEARED; }
    public int[][] getLastLockedBlocks()        { return lastLockedBlocks; }
    public int getLastLockedX()                 { return lastLockedX; }
    public int getLastLockedY()                 { return lastLockedY; }
}