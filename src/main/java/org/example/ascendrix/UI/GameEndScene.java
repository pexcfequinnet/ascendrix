
package org.example.ascendrix.UI;

import com.fasterxml.jackson.annotation.JsonCreator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.ascendrix.GameData.ScoreManager;
import org.example.ascendrix.GameMode.GameMode; // Nhớ import GameMode

import java.util.regex.Pattern;

public class GameEndScene {

    private final Scene scene;

    // ✅ ĐÃ SỬA: Nhận Mode, displayValue (Chuỗi hiển thị) và sortValue (Số để lưu Top)
    public GameEndScene(SceneManager manager, GameMode mode, boolean isClear, String displayValue, long sortValue) {

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1A1A1A;"); // Nền tối đồng bộ với game

        // 1. Tiêu đề Game Over / Game Clear
        Label titleLabel = new Label(isClear ? "G A M E   C L E A R !" : "G A M E   O V E R");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 40));
        titleLabel.setTextFill(isClear ? Color.web("#66BB6A") : Color.web("#EF5350"));

        // 2. ✅ ĐÃ SỬA: Hiển thị trực tiếp cái chuỗi displayValue (VD: "01:25.00" hoặc "GM")
        Label scoreLabel = new Label(displayValue);
        scoreLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        scoreLabel.setTextFill(Color.WHITE);

        // 3. Label hướng dẫn
        Label instructionLabel = new Label("ENTER YOUR NAME:");
        instructionLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        instructionLabel.setTextFill(Color.LIGHTGRAY);

        // 4. Ô nhập tên (TextField) - Giữ nguyên logic xịn xò của bạn
        TextField nameInput = new TextField();
        nameInput.setMaxWidth(200);
        nameInput.setAlignment(Pos.CENTER);
        nameInput.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        nameInput.setStyle(
                "-fx-background-color: #000000; " +
                        "-fx-text-fill: #00FFCC; " +
                        "-fx-border-color: #555555; " +
                        "-fx-border-width: 2px;"
        );

        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 8) return null;
            if (!pattern.matcher(newText).matches()) return null;
            change.setText(change.getText().toUpperCase());
            return change;
        });
        nameInput.setTextFormatter(formatter);

        // 5. Nút Xác nhận lưu điểm
        Button submitBtn = new Button("SUBMIT");
        submitBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        submitBtn.setStyle("-fx-background-color: #42A5F5; -fx-text-fill: white; -fx-cursor: hand;");
        submitBtn.setPadding(new Insets(10, 40, 10, 40));

// Xử lý khi nhấn nút Submit
        submitBtn.setOnAction(e -> {
            String playerName = nameInput.getText().trim();
            if (playerName.isEmpty()) {
                playerName = "STACKER";
            }

            // Lưu điểm
            ScoreManager scoreManager = new ScoreManager();
            scoreManager.addScore(mode.name(), playerName, sortValue, displayValue);

            // Chuyển sang ModeDetailScene để xem bảng điểm vừa cập nhật
            // (Lưu ý: Truyền đúng tham số constructor của ModeDetailScene trong project của bạn,
            // thường là manager và mode)
            ModeDetailScene detailScene = new ModeDetailScene(manager, mode);
            manager.setScene(detailScene.getScene());
        });
        // Cho phép ấn phím ENTER để Submit nhanh
        nameInput.setOnAction(e -> submitBtn.fire());

        root.getChildren().addAll(titleLabel, scoreLabel, instructionLabel, nameInput, submitBtn);
        javafx.application.Platform.runLater(nameInput::requestFocus);

        scene = new Scene(root, 1024, 768);
    }

    public Scene getScene() {
        return scene;
    }
}