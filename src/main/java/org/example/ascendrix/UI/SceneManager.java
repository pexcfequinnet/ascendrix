package org.example.ascendrix.UI;

import javafx.scene.Scene;
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

    // 1. Thêm hàm setScene chung để đổi màn hình
    public void setScene(Scene newScene) {
        if (stage != null) {
            stage.setScene(newScene);
        }
    }

    // 2. Thêm hàm showMenu để nút "BACK TO MENU" có thể gọi
    public void showMenu() {
        // Tạo lại MenuScene và gán nó lên cửa sổ
        MenuScene menu = new MenuScene(this);
        setScene(menu.getScene());
    }
    public void showModeDetail(GameMode mode) {
        ModeDetailScene detailScene = new ModeDetailScene(this, mode);
        this.stage.setScene(detailScene.scene); // Gọi trực tiếp biến stage của bạn
    }
    public void startGame(GameMode mode) {
        GameScene gameScene = new GameScene(this, mode, timer);
        stage.setScene(gameScene.getScene());
    }
}
