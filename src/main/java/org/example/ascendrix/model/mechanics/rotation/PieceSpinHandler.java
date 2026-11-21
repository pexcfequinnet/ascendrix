package org.example.ascendrix.model.mechanics.rotation;

import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.piece.ActivePiece;

public interface PieceSpinHandler {
    SpinType detect(ActivePiece piece, Board board);
}