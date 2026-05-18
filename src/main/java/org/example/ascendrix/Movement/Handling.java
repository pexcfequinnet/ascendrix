package org.example.ascendrix.Movement;

import org.example.ascendrix.Input.InputHandler;
import org.example.ascendrix.MainGame.Engine.GameEngine;

public interface Handling {
    void update(long now, InputHandler input, GameEngine game);
}
