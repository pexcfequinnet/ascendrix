package org.example.ascendrix.UI;

import javafx.stage.Stage;
import org.example.ascendrix.Movement.Handling;
import org.example.ascendrix.GameMode.GameMode;
import org.example.ascendrix.MainGame.Renderer.GameScene;
import org.example.ascendrix.MainGame.Engine.GameTimer;

public class SceneManager {
    private final GameTimer timer = new GameTimer();
    private final Stage stage;
    public Handling handling;
    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void showMenu() {
        MenuScene menu = new MenuScene(this);
        stage.setScene(menu.getScene());
    }

    public void startGame(GameMode mode) {
        GameScene gameScene = new GameScene(this, mode, timer);
        stage.setScene(gameScene.getScene());
    }
}
