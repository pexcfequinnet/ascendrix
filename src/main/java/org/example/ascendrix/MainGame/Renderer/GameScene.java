package org.example.ascendrix.MainGame.Renderer;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.example.ascendrix.GameMode.GameMode;
import org.example.ascendrix.GameMode.GameModeHandler;
import org.example.ascendrix.GameMode.ModeHandlerFactory;
import org.example.ascendrix.Input.InputHandler;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.UI.SceneManager;

public class GameScene {

    private final Scene scene;

    public GameScene(SceneManager manager, GameMode mode, GameTimer timer) {
        GameRenderer renderer = new GameRenderer();
        GameModeHandler modeHandler = ModeHandlerFactory.create(mode, timer);

        GameEngine engine = new GameEngine(renderer, modeHandler, mode);

        InputHandler input = new InputHandler(engine);
        engine.setInput(input);

        StackPane root = new StackPane(renderer);
        scene = new Scene(root, 800, 700);

        input.attach(scene);

        // ESC to menu
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                engine.stop();
                manager.showMenu();
            }

        });

        engine.start();
    }

    public Scene getScene() {
        return scene;
    }
}