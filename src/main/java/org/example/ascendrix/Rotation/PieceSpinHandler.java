package org.example.ascendrix.Rotation;

import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.Tetromino.TetrominoHandler;

public interface PieceSpinHandler {
    SpinType detect(TetrominoHandler piece, GameEngine game);
}
