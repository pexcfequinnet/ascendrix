package org.example.ascendrix.Tetromino;

public class TetrominoHandler {

    public int x, y;
    public TetrominoType type;
    public int rotation;
    public int[][] blocks;
    public boolean lastMoveWasRotation = false;
    public boolean fellBetweenRotations;
    public boolean kickWasHorizontal = false;
    public boolean movedAfterRotation = false;
    public boolean movedBeforeRotation;
    public boolean droppedByPlayer = false;
    public int lastKickIndex = -1;
    public int yAtRotation = -1;
    public boolean movedSinceLastRotation = true;
    public TetrominoHandler(TetrominoType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.rotation = 0;
        this.blocks = deepCopy(type.blocks);
    }
    public static int[][] deepCopy(int[][] arr) {
        int[][] copy = new int[arr.length][2];
        for (int i = 0; i < arr.length; i++) {
            copy[i][0] = arr[i][0];
            copy[i][1] = arr[i][1];
        }
        return copy;
    }

    public void applyRotation(int newX, int newY, int newRotation, int[][] newBlocks, int kickIndex, boolean kickHorizontal) {
        x = newX;
        y = newY;
        rotation = newRotation;
        blocks = newBlocks;
        lastKickIndex = kickIndex;
        kickWasHorizontal = kickHorizontal;
        fellBetweenRotations = droppedByPlayer;
        movedBeforeRotation = movedSinceLastRotation;
        lastMoveWasRotation = true;
        movedAfterRotation = false;
        movedSinceLastRotation = false;
        droppedByPlayer = false;
    }

    public int[][] getBlocks() {
        return blocks;
    }
}