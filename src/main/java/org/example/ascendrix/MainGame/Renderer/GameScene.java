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
import org.example.ascendrix.UI.GameEndScene;
import org.example.ascendrix.UI.SceneManager;

public class GameScene {

    private final Scene scene;

    public GameScene(SceneManager manager, GameMode mode, GameTimer timer) {
        GameRenderer renderer = new GameRenderer();
        GameModeHandler modeHandler = ModeHandlerFactory.create(mode, timer);

        GameEngine engine = new GameEngine(renderer, modeHandler, mode);

        InputHandler input = manager.getInputHandler();
        input.attachGame(engine);

        engine.setInput(input);

        // Đặt GameRenderer vào giữa màn hình
        StackPane root = new StackPane(renderer);

        // Cập nhật phông nền tối đồng bộ với tổng thể game
        root.setStyle("-fx-background-color: #1A1A1A;");

        // ✅ NÂNG CẤP: Độ phân giải chuẩn 1024x768
        scene = new Scene(root, 1024, 768);

        input.attach(scene);

        // Xử lý các phím tắt quản lý Scene (ESC và ENTER)
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {

            // 1. Phím ESC: Thoát game giữa chừng và về Menu
            if (e.getCode() == KeyCode.ESCAPE) {
                engine.stop();
                manager.showMenu();
            }

            // 2. Phím ENTER: Xác nhận kết thúc Game và tính điểm
            if (e.getCode() == KeyCode.ENTER) {
                boolean isOver = engine.isGameOver();
                boolean isClear = engine.isCleared();

                if (isOver || isClear) {
                    e.consume(); // Nuốt phím ENTER, không cho lọt sang màn sau
                    engine.stop();

                    // 🔥 LOGIC CHẶN SPRINT TẠI ĐÂY
                    // Nếu đang chơi SPRINT mà KHÔNG PHẢI là Clear (tức là Top Out)
                    if (mode.name().equals("SPRINT") && !isClear) {
                        System.out.println("Sprint Failed! Quay về Menu.");
                        manager.showMenu(); // Hoặc manager.setScene(new MenuScene(...))
                    }
                    else {
                        // Các trường hợp hợp lệ:
                        // - Clear Sprint
                        // - Marathon / Master / Overdrive (Top Out hay Clear đều có điểm để lưu)
                        String displayVal = modeHandler.getDisplayValue();
                        long sortVal = modeHandler.getSortValue();

                        manager.setScene(new GameEndScene(manager, mode, isClear, displayVal, sortVal).getScene());
                    }
                }
            }
        });

        // Bắt đầu vòng lặp game
        engine.start();
    }

    public Scene getScene() {
        return scene;
    }
}