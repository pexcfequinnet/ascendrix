package org.example.ascendrix;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

public class GameScene {

    private final Scene scene;
    private final GameEngine engine;

    public GameScene(SceneManager manager, GameMode mode) {
        GameRenderer renderer = new GameRenderer();
        RulesetHandler ruleset = RulesetFactory.create(mode);
        GameModeHandler modeHandler = ModeHandlerFactory.create(mode);

        engine = new GameEngine(renderer, ruleset, modeHandler);

        InputHandler input = new InputHandler(engine, engine::isRunning);
        engine.setInput(input);

        StackPane root = new StackPane(renderer);
        scene = new Scene(root, 500, 600);

        input.attach(scene);

        // ESC to menu
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) manager.showMenu();
        });

        engine.start();
    }

    public Scene getScene() {
        return scene;
    }
}