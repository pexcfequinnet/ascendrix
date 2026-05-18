package org.example.ascendrix.Rotation;

import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.Tetromino.TetrominoHandler;

public interface RotationSystem {
    void tryRotate(TetrominoHandler piece, int dir, GameEngine game);
}


