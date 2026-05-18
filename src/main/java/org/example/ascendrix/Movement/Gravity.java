package org.example.ascendrix.Movement;

import org.example.ascendrix.MainGame.Engine.GameEngine;

public interface Gravity {
    void update(long now, GameEngine game);

    void setFallNs(long ns);
}
