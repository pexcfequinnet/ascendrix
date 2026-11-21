package org.example.ascendrix.view.render;

import javafx.scene.canvas.GraphicsContext;
import org.example.ascendrix.model.mode.ModeRuleset;
import org.example.ascendrix.model.state.GameTimer;

public interface ModeHUDRenderer {
    void render(GraphicsContext gc, ModeRuleset ruleset, GameTimer timer, long now);
}