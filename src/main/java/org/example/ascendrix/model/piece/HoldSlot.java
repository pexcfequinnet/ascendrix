package org.example.ascendrix.model.piece;

public class HoldSlot {
    private TetrominoType heldType = null;
    private boolean usedThisTurn = false;
    private boolean inProgress = false;

    public boolean isEmpty() {
        return heldType == null;
    }

    public TetrominoType getHeldType() {
        return heldType;
    }

    public boolean canHold() {
        return !usedThisTurn && !inProgress;
    }

    public TetrominoType swap(TetrominoType incoming) {
        inProgress = true;
        TetrominoType previous = heldType;
        heldType = incoming;
        usedThisTurn = true;
        inProgress = false;
        return previous;
    }

    public void resetTurn() {
        usedThisTurn = false;
        inProgress = false;
    }
}