package org.example.ascendrix;

import javafx.stage.Stage;

public class SceneManager {
    private final GameTimer timer = new GameTimer();
    private final Stage stage;
    public Handling handling;
    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void showMenu() {
        MenuScene menu = new MenuScene(this, timer);
        stage.setScene(menu.getScene());
    }

    public void startGame(GameMode mode) {
        GameScene gameScene = new GameScene(this, mode, timer);
        stage.setScene(gameScene.getScene());
    }
}
