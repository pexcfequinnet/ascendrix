package org.example.ascendrix.controller;

import javafx.animation.AnimationTimer;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.view.render.GameRenderer;

import java.util.Objects;

public class GameLoopController {

    private final GameSession session;
    private final GameRenderer renderer;
    private final InputController inputController;
    private AnimationTimer gameLoop;

    public GameLoopController(GameSession session, GameRenderer renderer) {
        this(session, renderer, null);
    }

    public GameLoopController(GameSession session,
                              GameRenderer renderer,
                              InputController inputController) {
        this.session = Objects.requireNonNull(session, "GameSession cannot be null");
        this.renderer = Objects.requireNonNull(renderer, "GameRenderer cannot be null");
        this.inputController = inputController;
    }

    public void start() {
        stop();
        session.start(System.nanoTime());

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!session.isPaused()) {
                    int horizontal = (inputController != null) ? inputController.getHorizontal() : 0;
                    boolean softDrop = (inputController != null) && inputController.isSoftDropHeld();
                    session.update(now, horizontal, softDrop);
                }
                renderer.render(session, now);
            }
        };

        gameLoop.start();
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }

    public void pause() {
        if (inputController != null) {
            inputController.resetState();
        }
        session.pause();
    }

    public void resume() {
        session.resume(System.nanoTime());
    }

    public GameSession getSession() {
        return session;
    }
}