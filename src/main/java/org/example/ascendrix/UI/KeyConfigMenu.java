package org.example.ascendrix.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.ascendrix.Input.*;

import java.util.HashMap;
import java.util.Map;

public class KeyConfigMenu extends VBox {

    private final InputHandler inputHandler;
    private Button listeningButton = null;
    private InputAction listeningAction = null;

    // ✅ NÂNG CẤP: Dùng Map để lưu trữ nút, giúp refresh an toàn tuyệt đối dù thay đổi layout
    private final Map<InputAction, Button> buttonMap = new HashMap<>();

    public KeyConfigMenu(InputHandler inputHandler, Runnable onBackRequested) {
        this.inputHandler = inputHandler;

        // Căn chỉnh cho vừa chuẩn 1024x768
        this.setPrefSize(1024, 768);
        this.setSpacing(40); // Tăng khoảng cách các cụm chính
        this.setPadding(new Insets(50));
        this.setStyle("-fx-background-color: #1a1a1a;");
        this.setAlignment(Pos.CENTER);

        // 1. Tiêu đề Menu
        Label title = new Label("KEYBIND CONFIGURATION");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 40)); // Font to ra dáng Arcade
        title.setTextFill(Color.ORANGE);
        this.getChildren().add(title);

        // 2. Lưới chứa nút bấm (Chia 2 cột cho màn hình to)
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(80); // Khoảng cách giữa 2 cột
        grid.setVgap(20); // Khoảng cách giữa các hàng

        int row = 0;
        int col = 0;
        InputAction[] actions = InputAction.values();

        // Chia nửa số lượng Action để dàn đều 2 cột
        int itemsPerColumn = (int) Math.ceil(actions.length / 2.0);

        for (InputAction action : actions) {
            HBox rowBox = new HBox(30);
            rowBox.setAlignment(Pos.CENTER_LEFT);
            rowBox.setPrefWidth(350); // Mở rộng chiều ngang cho từng item

            // Tên hành động bên trái
            Label actionLabel = new Label(action.name().replace("_", " "));
            actionLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
            actionLabel.setTextFill(Color.LIGHTGRAY);
            actionLabel.setMinWidth(180);

            // Nút bấm cấu hình bên phải
            KeyCode currentKey = inputHandler.getKeyForAction(action);
            String keyName = (currentKey != null) ? currentKey.name() : "NONE";
            Button bindButton = new Button(keyName);
            bindButton.setFont(Font.font("System", FontWeight.BOLD, 16));
            bindButton.setPrefWidth(150);
            bindButton.setPrefHeight(40);
            bindButton.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

            // Lưu vào Map để lát nữa Update cho dễ
            buttonMap.put(action, bindButton);

            bindButton.setOnAction(e -> startListening(bindButton, action));

            rowBox.getChildren().addAll(actionLabel, bindButton);
            grid.add(rowBox, col, row);

            // Logic tính toán chia cột
            row++;
            if (row >= itemsPerColumn) {
                row = 0;
                col++;
            }
        }

        this.getChildren().add(grid);

        // 3. Nút Save
        Button backButton = new Button("SAVE & RETURN");
        backButton.setFont(Font.font("System", FontWeight.BOLD, 20)); // To và rõ ràng
        backButton.setStyle("-fx-background-color: orange; -fx-text-fill: black; -fx-cursor: hand; -fx-background-radius: 8;");
        backButton.setPrefWidth(300);
        backButton.setPrefHeight(50);

        backButton.setOnAction(e -> {
            inputHandler.saveKeybinds(); // Lưu vĩnh viễn
            if (onBackRequested != null) onBackRequested.run();
        });

        this.getChildren().add(backButton);
    }

    private void startListening(Button button, InputAction action) {
        if (listeningButton != null) {
            // Khôi phục nút cũ nếu người chơi click sang nút khác
            listeningButton.setText(inputHandler.getKeyForAction(listeningAction).toString());
            listeningButton.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 5;");
        }

        listeningButton = button;
        listeningAction = action;
        button.setText("[ PRESS KEY ]");
        button.setStyle("-fx-background-color: #ff4500; -fx-text-fill: white; -fx-background-radius: 5;");
    }

    public void handleSceneKeyEvent(javafx.scene.input.KeyEvent event) {
        if (listeningButton == null || listeningAction == null) return;

        // Xóa focus khỏi ô đang nghe (Tránh bị kẹt phím Enter, Space)
        this.requestFocus();

        inputHandler.bindKey(listeningAction, event.getCode());
        refreshAllButtons();

        listeningButton = null;
        listeningAction = null;
        event.consume();
    }

    // ✅ ĐÃ NÂNG CẤP: Quét thẳng vào Map thay vì duyệt Layout con, cực kỳ an toàn!
    private void refreshAllButtons() {
        for (Map.Entry<InputAction, Button> entry : buttonMap.entrySet()) {
            InputAction action = entry.getKey();
            Button btn = entry.getValue();

            KeyCode currentKey = inputHandler.getKeyForAction(action);
            btn.setText(currentKey != null ? currentKey.name() : "NONE");
            btn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 5;");
        }
    }

    public boolean isListening() {
        return listeningButton != null && listeningAction != null;
    }
}