package org.example.ascendrix.Rotation;

import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.Tetromino.TetrominoHandler;

public interface RotationSystem {
    boolean tryRotate(
            TetrominoHandler piece,
            RotationDirection dir,
            GameEngine game
    );
}


