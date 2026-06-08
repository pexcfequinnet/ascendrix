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
                        "-fx-font-size: 36px; " + // Tăng size chữ lên một chút cho hợp với màn hình to
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + themeColor + ";"
        );

        // 3. Cột trái: Configuration
        VBox configBox = new VBox(15); // Tăng khoảng cách các dòng cho thoáng
        configBox.setAlignment(Pos.TOP_LEFT);
        Label configTitle = new Label("⚙ CONFIGURATION");
        configTitle.setStyle("-fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label startingLevel = createInfoLabel("");
        if(mode != GameMode.SPRINT)
            startingLevel = createInfoLabel("Start Level: 1");
        Label dasConfig = createInfoLabel("DAS: Auto");
        Label arrConfig = createInfoLabel("ARR: Auto");
        configBox.getChildren().addAll(configTitle, startingLevel, dasConfig, arrConfig);

        // 4. Cột phải: High Scores
        VBox scoreBox = new VBox(10);
        scoreBox.setAlignment(Pos.TOP_LEFT);
        Label scoreTitle = new Label("HIGH SCORES");
        scoreTitle.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 16px;");
        scoreBox.getChildren().add(scoreTitle);

        ScoreManager scoreManager = new ScoreManager();
        List<ScoreRecord> topScores = scoreManager.getTopScores(mode.toString());

        if (topScores.isEmpty()) {
            // Nếu chưa có ai chơi mode này
            scoreBox.getChildren().add(createInfoLabel("No record."));
            scoreBox.getChildren().add(createInfoLabel("Become the first person to set a score"));
        } else {
            // ✅ ĐÃ SỬA: Vòng lặp hiển thị danh sách Top 10
            for (int i = 0; i < 10; i++) {
                if (i < topScores.size()) {
                    ScoreRecord record = topScores.get(i);
                    // Hiển thị chuẩn: 1. 01:25.450 - AAA hoặc 1. GM - DEV
                    // String format giúp số thứ tự (1-10) thẳng hàng hơn
                    String displayText = String.format("%2d. %s - %s", (i + 1), record.getDisplayValue(), record.getPlayerName());
                    scoreBox.getChildren().add(createInfoLabel(displayText));
                } else {
                    // Dòng trống cho những slot chưa có người lấp vào
                    String emptyText = String.format("%2d. ---", (i + 1));
                    scoreBox.getChildren().add(createInfoLabel(emptyText));
                }
            }
        }

        // Ghép 2 cột lại với nhau
        HBox centerContent = new HBox(80, configBox, scoreBox); // Tăng khoảng cách giữa 2 cột ra 80px cho cân đối màn hình 1024
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(40, 0, 40, 0));

        // 5. Nút bấm (Play và Back)
        Button playBtn = new Button("START GAME");
        Button backBtn = new Button("BACK TO MENU");

        styleActionBtn(playBtn, themeColor, "#FFFFFF");
        styleActionBtn(backBtn, "#555555", "#CCCCCC");

        playBtn.setOnAction(e -> manager.startGame(mode));
        backBtn.setOnAction(e -> manager.showMenu());

        // 6. Layout tổng
        VBox root = new VBox(25, titleLabel, centerContent, playBtn, backBtn);
        root.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 40;");
        root.setAlignment(Pos.CENTER);


        // ✅ ĐÃ SỬA: Kích thước Scene mới 1024x768
        scene = new Scene(root, 1024, 768);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                case ESCAPE:
                    manager.showMenu();
                    break;
                default:
                    break;
            }
        });
    }

    // --- CÁC HÀM HELPER ---

    private String getModeColor(GameMode mode) {
        return switch (mode) {
            case SPRINT -> "#00C8C8";
            case MARATHON -> "#66BB6A";
            case MASTER -> "#42A5F5";
            case OVERDRIVE -> "#EF5350";
            default -> "#FFFFFF";
        };
    }

    private Label createInfoLabel(String text) {
        Label lbl = new Label(text);
        // Tăng font size một chút xíu cho dễ đọc trên màn hình to
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-family: 'Courier New', monospace;");
        return lbl;
    }

    private void styleActionBtn(Button btn, String bgColor, String textColor) {
        String baseStyle =
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-pref-width: 250px; " + // Nút dài ra một chút cho cân với màn hình to
                        "-fx-pref-height: 45px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-cursor: hand; ";

        String idleStyle = baseStyle + "-fx-background-color: " + bgColor + ";";
        String hoverStyle = baseStyle + "-fx-background-color: derive(" + bgColor + ", 20%);";

        btn.setStyle(idleStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(idleStyle));
    }

    public Scene getScene() {
        return scene;
    }
}