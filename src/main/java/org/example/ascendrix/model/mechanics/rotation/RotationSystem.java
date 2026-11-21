package org.example.ascendrix.model.mechanics.rotation;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.piece.ActivePiece;

public interface RotationSystem {
    boolean tryRotate(ActivePiece piece, RotationDirection dir, Board board);
}