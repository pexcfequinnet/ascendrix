package org.example.ascendrix;

public interface Gravity {
    void update(long now, GameEngine game);

    void setFallNs(long ns);
}
