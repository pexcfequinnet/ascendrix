package org.example.ascendrix.view.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.ascendrix.view.render.BaseHUDRenderer;
import org.example.ascendrix.model.mode.GameMode;
import org.example.ascendrix.model.progression.ScoreManager;
import org.example.ascendrix.model.progression.ScoreRecord;

import java.util.List;

public class ModeDetailScene {

    private static final double WIDTH = 1024.0;
    private static final double HEIGHT = 768.0;
    private static final int LEADERBOARD_SLOTS = 10;

    private final Scene scene;

    public ModeDetailScene(SceneManager manager, GameMode mode) {
        String themeColor = getModeColor(mode);

        // 1. Mode Title
        Label titleLabel = new Label(mode.name() + " MODE");
        titleLabel.setStyle(
                "-fx-font-family: 'Monospaced', 'Courier New', monospace; " +
                        "-fx-font-size: 36px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + themeColor + ";"
        );

        // 2. Left Column: Mode Configuration
        VBox configBox = new VBox(15);
        configBox.setAlignment(Pos.TOP_LEFT);

        Label configTitle = new Label("CONFIGURATION");
        configTitle.setStyle("-fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-font-size: 16px;");
        configBox.getChildren().add(configTitle);

        if (mode != GameMode.SPRINT && mode != GameMode.ULTRA) {
            configBox.getChildren().add(createInfoLabel("Start Level: 1"));
        }
        configBox.getChildren().addAll(
                createInfoLabel("DAS: Auto"),
                createInfoLabel("ARR: Auto")
        );

        // 3. Right Column: Top 10 High Scores
        VBox scoreBox = new VBox(10);
        scoreBox.setAlignment(Pos.TOP_LEFT);

        Label scoreTitle = new Label("HIGH SCORES");
        scoreTitle.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 16px;");
        scoreBox.getChildren().add(scoreTitle);

        ScoreManager scoreManager = manager.getScoreManager();
        List<ScoreRecord> topScores = scoreManager.getTopScores(mode.name());

        if (topScores.isEmpty()) {
            scoreBox.getChildren().addAll(
                    createInfoLabel("No record."),
                    createInfoLabel("Become the first person to set a score")
            );
        } else {
            for (int i = 0; i < LEADERBOARD_SLOTS; i++) {
                if (i < topScores.size()) {
                    ScoreRecord record = topScores.get(i);
                    String timeStr = BaseHUDRenderer.formatTime(record.getElapsedMs());
                    String statText = (mode == GameMode.SPRINT)
                            ? timeStr
                            : record.getDisplayValue() + " (" + timeStr + ")";

                    String displayText = String.format("%2d. %s - %s", (i + 1), statText, record.getPlayerName());
                    scoreBox.getChildren().add(createInfoLabel(displayText));
                } else {
                    scoreBox.getChildren().add(createInfoLabel(String.format("%2d. ---", (i + 1))));
                }
            }
        }

        HBox centerContent = new HBox(80, configBox, scoreBox);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(40, 0, 40, 0));

        // 4. Action Buttons
        Button playBtn = new Button("START GAME");
        Button backBtn = new Button("BACK TO MENU");

        styleActionBtn(playBtn, themeColor, "#FFFFFF");
        styleActionBtn(backBtn, "#555555", "#CCCCCC");

        playBtn.setOnAction(e -> manager.startGame(mode));
        backBtn.setOnAction(e -> manager.showMenu());

        // 5. Root Layout & Scene
        VBox root = new VBox(25, titleLabel, centerContent, playBtn, backBtn);
        root.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 40;");
        root.setAlignment(Pos.CENTER);

        this.scene = new Scene(root, WIDTH, HEIGHT);

        // ESC returns to Main Menu; ENTER activates the focused button naturally
        this.scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                manager.showMenu();
                event.consume();
            }
        });
    }

    private String getModeColor(GameMode mode) {
        return switch (mode) {
            case SPRINT    -> "#00C8C8";
            case ULTRA     -> "#FFA726";
            case MARATHON  -> "#66BB6A";
            case MASTER    -> "#42A5F5";
            case OVERDRIVE -> "#EF5350";
        };
    }

    private Label createInfoLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-family: 'Monospaced', 'Courier New', monospace;");
        return lbl;
    }

    private void styleActionBtn(Button btn, String bgColor, String textColor) {
        String baseStyle =
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-pref-width: 250px; " +
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