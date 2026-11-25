package org.example.ascendrix.model.piece;

import java.util.Objects;

public class ActivePiece {

    private final TetrominoType type;
    private int x;
    private int y;
    private int rotation;
    private int[][] blocks;

    // Rotation & spin-detection telemetry
    private boolean lastMoveWasRotation = false;
    private boolean fellBetweenRotations = false;
    private boolean kickWasHorizontal = false;
    private boolean movedAfterRotation = false;
    private boolean movedBeforeRotation = false;
    private boolean droppedByPlayer = false;
    private int lastKickIndex = -1;
    private int yAtRotation = -1;
    private boolean movedSinceLastRotation = true;

    public ActivePiece(TetrominoType type, int x, int y) {
        this.type = Objects.requireNonNull(type, "TetrominoType cannot be null");
        this.x = x;
        this.y = y;
        this.rotation = 0;
        this.blocks = deepCopy(type.blocks);
    }

    /* ========================== MOVEMENT & POSITION ========================== */

    public void setY(int y) {
        this.y = y;
    }

    public void moveHorizontal(int dx) {
        if (dx == 0) return;
        this.x += dx;
        this.lastMoveWasRotation = false;
        this.movedAfterRotation = true;
        this.movedSinceLastRotation = true;
    }

    public void moveDown(boolean byPlayer) {
        this.y += 1;
        if (byPlayer) {
            this.droppedByPlayer = true;
            this.lastMoveWasRotation = false;
            this.movedAfterRotation = true;
            this.movedSinceLastRotation = true;
        }
    }

    public void pushUp() {
        this.y -= 1;
    }

    public void markSpawnMovementComplete() {
        if (!lastMoveWasRotation) {
            this.movedAfterRotation = false;
            this.movedBeforeRotation = false;
            this.movedSinceLastRotation = true;
        }
        this.droppedByPlayer = false;
    }

    /* ========================== ROTATION ========================== */

    public void applyRotation(int newX,
                              int newY,
                              int newRotation,
                              int[][] newBlocks,
                              int kickIndex,
                              boolean kickHorizontal) {
        this.x = newX;
        this.y = newY;
        this.rotation = Math.floorMod(newRotation, 4);
        this.blocks = deepCopy(newBlocks);
        this.lastKickIndex = kickIndex;
        this.kickWasHorizontal = kickHorizontal;
        this.fellBetweenRotations = this.droppedByPlayer;
        this.movedBeforeRotation = this.movedSinceLastRotation;
        this.lastMoveWasRotation = true;
        this.movedAfterRotation = false;
        this.movedSinceLastRotation = false;
        this.droppedByPlayer = false;
    }

    public void onRotated() {
        this.yAtRotation = this.y;
        this.lastMoveWasRotation = true;
    }

    /* ========================== QUERIES & HELPERS ========================== */

    public TetrominoType getType()              { return type; }
    public int getX()                           { return x; }
    public int getY()                           { return y; }
    public int getRotation()                    { return rotation; }
    public int[][] getBlocks()                  { return blocks; }

    public boolean isLastMoveWasRotation()      { return lastMoveWasRotation; }
    public boolean isFellBetweenRotations()     { return fellBetweenRotations; }
    public boolean isKickWasHorizontal()        { return kickWasHorizontal; }
    public boolean isMovedAfterRotation()       { return movedAfterRotation; }
    public boolean isMovedBeforeRotation()      { return movedBeforeRotation; }
    public boolean isDroppedByPlayer()          { return droppedByPlayer; }
    public int getLastKickIndex()               { return lastKickIndex; }
    public int getYAtRotation()                 { return yAtRotation; }
    public boolean isMovedSinceLastRotation()   { return movedSinceLastRotation; }

    public static int[][] deepCopy(int[][] arr) {
        int[][] copy = new int[arr.length][2];
        for (int i = 0; i < arr.length; i++) {
            copy[i][0] = arr[i][0];
            copy[i][1] = arr[i][1];
        }
        return copy;
    }

}

