package org.example.ascendrix.view.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.ascendrix.controller.StorageController;
import org.example.ascendrix.model.mode.GameMode;
import org.example.ascendrix.model.progression.ScoreManager;
import org.example.ascendrix.model.state.GameSession.GameResult;
import org.example.ascendrix.view.render.BaseHUDRenderer;

import java.util.regex.Pattern;

public class GameEndScene {

    private static final double WIDTH = 1024.0;
    private static final double HEIGHT = 768.0;
    private static final int MAX_NAME_LENGTH = 8;
    private static final String DEFAULT_NAME = "STACKER";
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]*$");

    private final Scene scene;
    private boolean submitted = false;

    public GameEndScene(SceneManager manager,
                        ScoreManager scoreManager,
                        StorageController storageController,
                        GameMode mode,
                        boolean isClear,
                        GameResult result) {
        this(
                manager,
                scoreManager,
                storageController,
                mode,
                isClear,
                (result != null) ? result.displayText() : "",
                (result != null) ? result.asSortValue() : 0L,
                (result != null) ? result.elapsedMs() : 0L
        );
    }

    public GameEndScene(SceneManager manager,
                        ScoreManager scoreManager,
                        StorageController storageController,
                        GameMode mode,
                        boolean isClear,
                        String displayValue,
                        long sortValue) {
        this(manager, scoreManager, storageController, mode, isClear, displayValue, sortValue, 0L);
    }

    public GameEndScene(SceneManager manager,
                        ScoreManager scoreManager,
                        StorageController storageController,
                        GameMode mode,
                        boolean isClear,
                        String displayValue,
                        long sortValue,
                        long elapsedMs) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1A1A1A;");

        boolean isSprintFail = (mode == GameMode.SPRINT && !isClear);

        // 1. Game Over / Game Clear Header
        Label titleLabel = new Label(isClear ? "G A M E   C L E A R !" : "G A M E   O V E R");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 40));
        titleLabel.setTextFill(isClear ? Color.web("#66BB6A") : Color.web("#EF5350"));

        // 2. Final Result Display
        String formattedTime = BaseHUDRenderer.formatTime(elapsedMs);
        String headlineText;
        if (isSprintFail) {
            headlineText = "DNF  (" + formattedTime + ")";
        } else if (mode == GameMode.SPRINT || displayValue == null || displayValue.isBlank()) {
            headlineText = formattedTime;
        } else {
            headlineText = displayValue + " (" + formattedTime + ")";
        }

        Label scoreLabel = new Label(headlineText);
        scoreLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        scoreLabel.setTextFill(isSprintFail ? Color.web("#EF5350") : Color.WHITE);

        root.getChildren().addAll(titleLabel, scoreLabel);

        if (isSprintFail) {
            // Sprint top-out: do not record a time on the leaderboard
            Label dnfNote = new Label("Clear all 40 lines to record a time.");
            dnfNote.setFont(Font.font("Consolas", FontWeight.NORMAL, 16));
            dnfNote.setTextFill(Color.LIGHTGRAY);

            Button retryBtn = new Button("RETRY");
            retryBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
            retryBtn.setStyle("-fx-background-color: #00C8C8; -fx-text-fill: white; -fx-cursor: hand;");
            retryBtn.setPadding(new Insets(10, 36, 10, 36));
            retryBtn.setOnAction(e -> manager.startGame(mode));

            Button backBtn = new Button("BACK");
            backBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
            backBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-cursor: hand;");
            backBtn.setPadding(new Insets(10, 36, 10, 36));
            backBtn.setOnAction(e -> manager.showModeDetail(mode));

            HBox actions = new HBox(18, retryBtn, backBtn);
            actions.setAlignment(Pos.CENTER);

            root.getChildren().addAll(dnfNote, actions);
            Platform.runLater(retryBtn::requestFocus);
        } else {
            // 3. Name Prompt
            Label instructionLabel = new Label("ENTER YOUR NAME:");
            instructionLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
            instructionLabel.setTextFill(Color.LIGHTGRAY);

            // 4. Arcade Name Input (Max 8 uppercase alphanumeric characters)
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

            TextFormatter<String> formatter = new TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.length() > MAX_NAME_LENGTH) return null;
                if (!ALPHANUMERIC_PATTERN.matcher(newText).matches()) return null;
                change.setText(change.getText().toUpperCase());
                return change;
            });
            nameInput.setTextFormatter(formatter);

            // 5. Submit Button
            Button submitBtn = new Button("SUBMIT");
            submitBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
            submitBtn.setStyle("-fx-background-color: #42A5F5; -fx-text-fill: white; -fx-cursor: hand;");
            submitBtn.setPadding(new Insets(10, 40, 10, 40));

            submitBtn.setOnAction(e -> {
                if (submitted) return;
                submitted = true;
                submitBtn.setDisable(true);

                String playerName = nameInput.getText().trim();
                if (playerName.isEmpty()) {
                    playerName = DEFAULT_NAME;
                }

                storageController.recordAndSaveScore(
                        scoreManager,
                        mode.name(),
                        playerName,
                        sortValue,
                        displayValue,
                        elapsedMs
                );
                manager.showModeDetail(mode);
            });
            nameInput.setOnAction(e -> submitBtn.fire());
            root.getChildren().addAll(instructionLabel, nameInput, submitBtn);
            Platform.runLater(nameInput::requestFocus);
        }
        this.scene = new Scene(root, WIDTH, HEIGHT);
    }
    public Scene getScene() {
        return scene;
    }
}