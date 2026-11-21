package org.example.ascendrix.view.render;

import javafx.scene.canvas.GraphicsContext;
import org.example.ascendrix.model.mode.GameMode;
import org.example.ascendrix.model.mode.ModeRuleset;
import org.example.ascendrix.model.state.GameTimer;

import java.util.EnumMap;
import java.util.Map;

public class HUDRenderDispatcher {

    private final Map<GameMode, ModeHUDRenderer> renderers = new EnumMap<>(GameMode.class);

    public HUDRenderDispatcher() {
        renderers.put(GameMode.SPRINT, new SprintHUDRenderer());
        renderers.put(GameMode.ULTRA, new UltraHUDRenderer());
        renderers.put(GameMode.MARATHON, new MarathonHUDRenderer());
        renderers.put(GameMode.MASTER, new MasterHUDRenderer());
        renderers.put(GameMode.OVERDRIVE, new OverdriveHUDRenderer());
    }

    public void render(GraphicsContext gc, ModeRuleset ruleset, GameTimer timer, long now) {
        if (ruleset == null) return;

        ModeHUDRenderer renderer = renderers.get(ruleset.getMode());
        if (renderer != null) {
            renderer.render(gc, ruleset, timer, now);
        }
    }
}