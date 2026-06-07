package org.example.ascendrix.MainGame.Renderer;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
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
    private boolean isPaused = false;

    public GameScene(SceneManager manager, GameMode mode, GameTimer timer) {
        GameRenderer renderer = new GameRenderer();
        GameModeHandler modeHandler = ModeHandlerFactory.create(mode, timer);

        GameEngine engine = new GameEngine(renderer, modeHandler, mode);

        InputHandler input = manager.getInputHandler();
        input.attachGame(engine);

        engine.setInput(input);



        // ✅ NÂNG CẤP: Độ phân giải chuẩn 1024x768
        // =================================================================
        // TẠO GIAO DIỆN LỚP PHỦ PAUSE (OVERLAY)
        // =================================================================
        VBox pauseOverlay = new VBox(20);
        pauseOverlay.setAlignment(Pos.CENTER);
        pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);"); // Đen mờ 75%
        pauseOverlay.setVisible(false); // Ban đầu ẩn đi

        Label pauseLabel = new Label("P A U S E D");
        pauseLabel.setFont(Font.font("System", FontWeight.BOLD, 50));
        pauseLabel.setTextFill(Color.WHITE);

        Button resumeBtn = new Button("RESUME");
        resumeBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        resumeBtn.setStyle("-fx-background-color: #42A5F5; -fx-text-fill: white; -fx-cursor: hand;");
        resumeBtn.setPrefWidth(200);

        Button quitBtn = new Button("QUIT");
        quitBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        quitBtn.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white; -fx-cursor: hand;");
        quitBtn.setPrefWidth(200);

        pauseOverlay.getChildren().addAll(pauseLabel, resumeBtn, quitBtn);

        // Đặt GameRenderer vào giữa màn hình
        StackPane root = new StackPane(renderer, pauseOverlay);

        // Cập nhật phông nền tối đồng bộ với tổng thể game
        root.setStyle("-fx-background-color: #1A1A1A;");

        scene = new Scene(root, 1024, 768);
        input.attach(scene);

        // Xử lý sự kiện bấm nút trên Pause Menu
        resumeBtn.setOnAction(e -> {
            isPaused = false;
            pauseOverlay.setVisible(false);
            engine.resume(); // Trở lại game
            root.requestFocus(); // Trả lại focus cho Scene để tiếp tục nhận phím di chuyển
        });

        quitBtn.setOnAction(e -> {
            engine.stop();
            manager.showMenu();
        });

        // =================================================================
        // XỬ LÝ SỰ KIỆN BÀN PHÍM
        // =================================================================
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

            // 1. Phím ESC: Bật / Tắt Pause
            if (e.getCode() == KeyCode.ESCAPE) {

                if (engine.isGameOver() || engine.isCleared()) return;

                isPaused = !isPaused;
                if (isPaused) {
                    engine.pause(); // Đổi state trong Engine
                    pauseOverlay.setVisible(true);

                    // 🔥 THÊM DÒNG NÀY: Ép Menu nổi lên layer trên cùng của màn hình
                    pauseOverlay.toFront();

                } else {
                    pauseOverlay.setVisible(false);
                    engine.resume();
                }
                e.consume();
                return;
            }

            // 2. Phím ENTER: Xác nhận kết thúc Game và tính điểm
            if (e.getCode() == KeyCode.ENTER) {
                boolean isOver = engine.isGameOver();
                boolean isClear = engine.isCleared();

                if (isOver || isClear) {
                    e.consume();
                    engine.stop();

                    if (mode.name().equals("SPRINT") && !isClear) {
                        manager.showMenu();
                    }
                    else {
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