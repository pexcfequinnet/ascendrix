package org.example.ascendrix;

import javafx.stage.Stage;

public class SceneManager {
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
        GameScene gameScene = new GameScene(this, mode);
        stage.setScene(gameScene.getScene());
    }
}
