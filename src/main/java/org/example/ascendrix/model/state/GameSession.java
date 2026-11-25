package org.example.ascendrix.model.state;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.board.FadeMap;
import org.example.ascendrix.model.input.InputBuffer;
import org.example.ascendrix.model.input.KeyBindingConfig.InputAction;
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
    private static final long COUNTDOWN_STEP_NS = 1_000_000_000L;

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
    private long lastInputTick = -1L;

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
        phaseBeforePause = GamePhase.COUNTDOWN;
        lastTick = now;
        lastInputTick = now;
    }

    public void pause() {
        if (phase == GamePhase.COUNTDOWN || phase.isPlayable()) {
            phaseBeforePause = phase;
            phase = GamePhase.PAUSED;
            timer.pause();
        }
    }

    public void resume(long now) {
        if (phase != GamePhase.PAUSED) {
            return;
        }
        phase = phaseBeforePause;
        lastTick = now;
        lastInputTick = now;
        if (phase.isPlayable()) {
            timer.resume(now);
        }
    }

    /**
     * 1000Hz (1ms) input & handling tick: drains queued tap actions in chronological order
     * and updates DAS, ARR, DCD, and SDF at 1ms resolution.
     */
    public void stepInput(long nowNs, int horizontalDir, boolean softDropHeld) {
        if (!phase.isPlayable()) {
            return;
        }
        lastInputTick = nowNs;

        if (!canControlPiece()) {
            return;
        }

        InputBuffer.TimedInput event;
        while ((event = inputBuffer.pollActionUpTo(nowNs)) != null) {
            applyDiscreteAction(event.action(), nowNs);
            if (!canControlPiece() || !phase.isPlayable()) {
                return;
            }
        }

        ruleset.getHandling().update(nowNs, horizontalDir, softDropHeld, this);
    }

    /**
     * 60Hz (16.67ms) simulation tick: advances countdown, ruleset timers, board fade,
     * line-clear animation, ARE spawn delay, gravity, and lock delay.
     */
    public void stepSimulation(long nowNs) {
        if (phase == GamePhase.COUNTDOWN) {
            updateCountdown(nowNs);
            return;
        }
        if (!phase.isPlayable()) {
            return;
        }

        lastTick = nowNs;

        board.setFadeMap(ruleset.getFadeMap());
        ruleset.update(nowNs, this);

        board.updateFade(nowNs);
        if (board.isFading()) {
            return;
        }

        if (waitingForClearAnim) {
            handleClearAnim(nowNs);
            return;
        }

        handleSpawnDelay(nowNs);

        if (!waitingToSpawn && current != null) {
            ruleset.getGravity().update(nowNs, this);
            ruleset.getLockDelay().update(nowNs, this);
        }
    }

    /** Single-rate fallback when driven outside the dual-rate loop. */
    public void update(long now) {
        update(now, 0, false);
    }

    public void update(long now, int horizontalDir, boolean softDropHeld) {
        stepInput(now, horizontalDir, softDropHeld);
        stepSimulation(now);
    }

    public void clearGame() {
        phase = GamePhase.CLEARED;
        timer.pause();
    }

    public void topOut() {
        if (ruleset.isRollActive()) {
            clearGame();
        } else {
            gameOver();
        }
    }

    public void gameOver() {
        phase = GamePhase.GAME_OVER;
        timer.pause();
    }

    private void applyDiscreteAction(InputAction action, long nowNs) {
        switch (action) {
            case LEFT       -> move(-1, nowNs);
            case RIGHT      -> move(1, nowNs);
            case ROTATE_CCW -> rotate(RotationDirection.CCW, nowNs);
            case ROTATE_CW  -> rotate(RotationDirection.CW, nowNs);
            case ROTATE_180 -> rotate(RotationDirection.FLIP, nowNs);
            case HOLD       -> hold(nowNs);
            case HARD_DROP  -> hardDrop(nowNs);
            case SOFT_DROP  -> {} // Handled continuously by MovementProcessor via softDropHeld
        }
    }

    private boolean canControlPiece() {
        return !waitingToSpawn && !waitingForClearAnim && !board.isFading() && current != null;
    }

    /* ========================== SPAWN & HOLD ========================== */

    private int getSpawnYOffset(TetrominoType type) {
        return (type == TetrominoType.I) ? 4 : 5;
    }

    private void spawnBlock(long now) {
        spawnBlock(now, true);
    }

    private void spawnBlock(long now, boolean isTurnStart) {
        TetrominoType type = (pendingSpawnType != null) ? pendingSpawnType : queue.next();
        pendingSpawnType = null;

        if (isTurnStart) {
            holdSlot.resetTurn();

            // IHS (Initial Hold System)
            if (ruleset.supportsIHS() && holdSlot.canHold() && inputBuffer.isHoldHeld()) {
                if (holdSlot.isEmpty()) {
                    holdSlot.swap(type);
                    type = queue.next();
                } else {
                    type = holdSlot.swap(type);
                }
            }
        }

        int spawnY = getSpawnYOffset(type);
        current = new ActivePiece(type, SPAWN_X, spawnY);

        if (!board.canPlace(current.getBlocks(), current.getX(), current.getY())
                || !placeFromBottomIf20G(spawnY)) {
            topOut();
            return;
        }

        // IRS (Initial Rotation System)
        if (ruleset.supportsIRS()) {
            RotationDirection irs = inputBuffer.getHeldIRS(now);
            if (irs != null) {
                rotate(irs, now);
            }
        }

        // IMS (Initial Movement System)
        boolean movedOnSpawn = false;
        int dir = inputBuffer.getHeldIMS(now);
        if (dir != 0) {
            move(dir, now);
            movedOnSpawn = true;
        }

        // Charged DAS carry-over into newly spawned piece
        MovementProcessor movement = ruleset.getHandling();
        if (movement.isDasCharged(now)) {
            int dasDir = movement.getDirection();
            if (movement.getConfig().isInstantArr()) {
                while (canMove(current.getX() + dasDir, current.getY())) {
                    move(dasDir, now);
                }
            } else if (!movedOnSpawn) {
                move(dasDir, now);
            }
        }

        current.markSpawnMovementComplete();
    }

    public void hold() {
        hold(currentNow());
    }

    public void hold(long now) {
        if (!canControlPiece() || !holdSlot.canHold()) {
            return;
        }

        TetrominoType currentType = current.getType();
        if (holdSlot.isEmpty()) {
            holdSlot.swap(currentType);
            spawnBlock(now, false);
        } else {
            TetrominoType swapped = holdSlot.swap(currentType);
            int spawnY = getSpawnYOffset(swapped);
            current = new ActivePiece(swapped, SPAWN_X, spawnY);

            if (!board.canPlace(current.getBlocks(), current.getX(), current.getY())
                    || !placeFromBottomIf20G(spawnY)) {
                topOut();
                return;
            }
            current.markSpawnMovementComplete();
        }
    }

    /**
     * Places the active piece directly onto its bottom-up landing row when in 20G.
     * Returns false if the piece cannot legally rest on the board (top-out).
     */
    public boolean placeFromBottomIf20G(int spawnY) {
        if (current == null || !ruleset.getGravity().is20G()) {
            return true;
        }
        int landingY = board.findBottomUpLandingY(current.getBlocks(), current.getX(), spawnY);
        if (landingY == Integer.MIN_VALUE) {
            return false;
        }
        current.setY(landingY);
        return true;
    }

    /**
     * Snaps the active piece immediately to the bottom-up surface of its current columns
     * after horizontal shifts, rotations, or mid-piece 20G transitions.
     */
    public void snapCurrentToBottomIfNeeded() {
        if (current == null || !ruleset.getGravity().is20G()) {
            return;
        }
        int landingY = board.findBottomUpLandingY(current.getBlocks(), current.getX(), current.getY());
        if (landingY != Integer.MIN_VALUE && landingY > current.getY()) {
            current.setY(landingY);
        }
    }

    /* ========================== MOVEMENT & ROTATION ========================== */

    public void move(int dir) {
        move(dir, currentNow());
    }

    public void move(int dir, long now) {
        if (!canControlPiece()) return;
        if (canMove(current.getX() + dir, current.getY())) {
            current.moveHorizontal(dir);
            snapCurrentToBottomIfNeeded();
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

    public void rotate180() {
        rotate(RotationDirection.FLIP, currentNow());
    }

    public void rotate(RotationDirection dir, long now) {
        if (!canControlPiece()) return;

        if (ruleset.getRotationSystem().tryRotate(current, dir, board)) {
            snapCurrentToBottomIfNeeded();
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
            ruleset.getHandling().resetDAS();
            spawnBlock(now);
            ruleset.onPieceSpawned(this);
        }
    }

    private void updateCountdown(long now) {
        if (now - lastTick >= COUNTDOWN_STEP_NS) {
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
        if (lastInputTick > 0L) return lastInputTick;
        if (lastTick > 0L) return lastTick;
        return System.nanoTime();
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
        if (ruleset.getGravity().is20G()) {
            return current.getY();
        }
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

    public GameResult getResult() {
        long elapsed = timer.getElapsedMs();
        if (ruleset == null) {
            return new GameResult("", 0L, elapsed);
        }
        GameResult base = ruleset.getResult();
        long finalElapsed = (base.elapsedMs() > 0L) ? base.elapsedMs() : elapsed;
        return new GameResult(base.displayText(), base.rawValue(), finalElapsed);
    }

    /* ========================== GETTERS ========================== */

    public Board getBoard()                { return board; }
    public ActivePiece getCurrentPiece()   { return current; }
    public TetrominoQueue getQueue()       { return queue; }
    public TetrominoType getHoldPiece()    { return holdSlot.getHeldType(); }
    public GameMode getMode()              { return mode; }
    public ModeRuleset getRuleset()        { return ruleset; }
    public InputBuffer getInputBuffer()    { return inputBuffer; }
    public GameTimer getTimer()            { return timer; }
    public GamePhase getPhase()            { return phase; }
    public int getCountdown()              { return countdown; }
    public int getTotalLines()             { return totalLines; }
    public boolean isSpawning()            { return waitingToSpawn; }
    public boolean isWaitingForClearAnim() { return waitingForClearAnim; }
    public boolean isPaused()              { return phase == GamePhase.PAUSED; }
    public boolean isGameOver()            { return phase == GamePhase.GAME_OVER; }
    public boolean isCleared()             { return phase == GamePhase.CLEARED; }
    public int[][] getLastLockedBlocks()   { return lastLockedBlocks; }
    public int getLastLockedX()            { return lastLockedX; }
    public int getLastLockedY()            { return lastLockedY; }
}