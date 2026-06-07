package org.example.ascendrix.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.ascendrix.GameData.ScoreManager;
import org.example.ascendrix.GameData.ScoreRecord;
import org.example.ascendrix.GameMode.GameMode;

import java.util.List;

public class ModeDetailScene {
    public Scene scene;

    public ModeDetailScene(SceneManager manager, GameMode mode) {
        // 1. Lấy màu chủ đạo dựa trên GameMode
        String themeColor = getModeColor(mode);

        // 2. Tiêu đề Mode
        Label titleLabel = new Label(mode.toString() + " MODE");
        titleLabel.setStyle(
                "-fx-font-family: 'Courier New', monospace; " +
                        "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + themeColor + ";"
        );

        // 3. Cột trái: Configuration
        VBox configBox = new VBox(10);
        configBox.setAlignment(Pos.TOP_LEFT);
        Label configTitle = new Label("⚙ CONFIGURATION");
        configTitle.setStyle("-fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label startingLevel = createInfoLabel("Start Level: 1");
        Label dasConfig = createInfoLabel("DAS: Auto");
        Label arrConfig = createInfoLabel("ARR: Auto");
        configBox.getChildren().addAll(configTitle, startingLevel, dasConfig, arrConfig);

        VBox scoreBox = new VBox(10);
        scoreBox.setAlignment(Pos.TOP_LEFT);
        Label scoreTitle = new Label("🏆 HIGH SCORES");
        scoreTitle.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 14px;");
        scoreBox.getChildren().add(scoreTitle);

        // --- BẮT ĐẦU DÙNG DỮ LIỆU THẬT ---
        ScoreManager scoreManager = new ScoreManager();
        List<ScoreRecord> topScores = scoreManager.getTopScores(mode.toString());

        if (topScores.isEmpty()) {
            // Nếu chưa có ai chơi mode này
            scoreBox.getChildren().add(createInfoLabel("Chưa có kỷ lục nào."));
            scoreBox.getChildren().add(createInfoLabel("Hãy là người đầu tiên!"));
        } else {
            // In danh sách Top 5 (hoặc ít hơn nếu chưa đủ 5 người)
            for (int i = 0; i < 5; i++) {
                if (i < topScores.size()) {
                    ScoreRecord record = topScores.get(i);
                    // Hiển thị chuẩn: 1. 01:25.450 - AAA hoặc 1. GM - DEV
                    String displayText = (i + 1) + ". " + record.getDisplayValue() + " - " + record.getPlayerName();
                    scoreBox.getChildren().add(createInfoLabel(displayText));
                } else {
                    // Dòng trống cho những slot chưa có người lấp vào
                    scoreBox.getChildren().add(createInfoLabel((i + 1) + ". ---"));
                }
            }
        }

        // Ghép 2 cột lại với nhau
        HBox centerContent = new HBox(40, configBox, scoreBox);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(30, 0, 30, 0)); // Tạo khoảng trống trên dưới

        // 5. Nút bấm (Play và Back)
        Button playBtn = new Button("START GAME");
        Button backBtn = new Button("BACK TO MENU");

        styleActionBtn(playBtn, themeColor, "#FFFFFF"); // Nút Play dùng màu của Mode
        styleActionBtn(backBtn, "#555555", "#CCCCCC");  // Nút Back màu xám

        // Xử lý sự kiện chuyển Scene
        playBtn.setOnAction(e -> manager.startGame(mode));
        // Giả sử SceneManager có hàm showMenu() để quay lại
        backBtn.setOnAction(e -> manager.showMenu());

        // 6. Layout tổng
        VBox root = new VBox(20, titleLabel, centerContent, playBtn, backBtn);
        root.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 30;");
        root.setAlignment(Pos.CENTER);

        scene = new Scene(root, 800, 600);
    }

    // --- CÁC HÀM HELPER ---

    private String getModeColor(GameMode mode) {
        return switch (mode) {
            case MARATHON -> "#66BB6A";
            case MASTER -> "#42A5F5";
            case OVERDRIVE -> "#EF5350";
            default -> "#FFFFFF"; // SPRINT hoặc mặc định
        };
    }

    private Label createInfoLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-family: 'Courier New', monospace;");
        return lbl;
    }

    private void styleActionBtn(Button btn, String bgColor, String textColor) {
        String baseStyle =
                "-fx-font-size: 15px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-pref-width: 200px; " +
                        "-fx-pref-height: 40px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-cursor: hand; ";

        String idleStyle = baseStyle + "-fx-background-color: " + bgColor + ";";
        // Làm màu hover sáng hơn một chút bằng cách giảm opacity hoặc đổi màu (ở đây mình dùng CSS đơn giản)
        String hoverStyle = baseStyle + "-fx-background-color: derive(" + bgColor + ", 20%);";

        btn.setStyle(idleStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(idleStyle));
    }
}