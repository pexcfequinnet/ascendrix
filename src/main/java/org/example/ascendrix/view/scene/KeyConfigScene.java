package org.example.ascendrix.view.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.ascendrix.controller.InputController;
import org.example.ascendrix.model.input.KeyBindingConfig.InputAction;

import java.util.EnumMap;
import java.util.Map;

public class KeyConfigScene {

    private static final double WIDTH = 1024.0;
    private static final double HEIGHT = 768.0;

    private static final String BTN_DEFAULT_STYLE =
            "-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;";
    private static final String BTN_LISTENING_STYLE =
            "-fx-background-color: #ff4500; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;";

    private final InputController inputController;
    private final Runnable onBackRequested;
    private final Map<InputAction, Button> buttonMap = new EnumMap<>(InputAction.class);

    private final VBox root;
    private final Scene scene;

    private Button listeningButton = null;
    private InputAction listeningAction = null;

    public KeyConfigScene(SceneManager manager, InputController inputController) {
        this(inputController, manager::showMenu);
    }

    public KeyConfigScene(InputController inputController, Runnable onBackRequested) {
        this.inputController = inputController;
        this.onBackRequested = onBackRequested;

        this.root = new VBox(40);
        this.root.setPrefSize(WIDTH, HEIGHT);
        this.root.setPadding(new Insets(50));
        this.root.setStyle("-fx-background-color: #1a1a1a;");
        this.root.setAlignment(Pos.CENTER);

        // 1. Title
        Label title = new Label("KEYBIND CONFIGURATION");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 40));
        title.setTextFill(Color.ORANGE);

        // 2. Two-Column Action Grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(80);
        grid.setVgap(20);

        InputAction[] actions = InputAction.values();
        int itemsPerColumn = (int) Math.ceil(actions.length / 2.0);
        int row = 0;
        int col = 0;

        for (InputAction action : actions) {
            HBox rowBox = new HBox(30);
            rowBox.setAlignment(Pos.CENTER_LEFT);
            rowBox.setPrefWidth(350);

            Label actionLabel = new Label(action.name().replace("_", " "));
            actionLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
            actionLabel.setTextFill(Color.LIGHTGRAY);
            actionLabel.setMinWidth(180);

            KeyCode currentKey = inputController.getKeyForAction(action);
            Button bindButton = new Button(formatKeyName(currentKey));
            bindButton.setFont(Font.font("System", FontWeight.BOLD, 16));
            bindButton.setPrefWidth(150);
            bindButton.setPrefHeight(40);
            bindButton.setStyle(BTN_DEFAULT_STYLE);

            buttonMap.put(action, bindButton);
            bindButton.setOnAction(e -> startListening(bindButton, action));

            rowBox.getChildren().addAll(actionLabel, bindButton);
            grid.add(rowBox, col, row);

            row++;
            if (row >= itemsPerColumn) {
                row = 0;
                col++;
            }
        }

        // 3. Bottom Action Bar
        Button resetButton = new Button("RESET DEFAULTS");
        resetButton.setFont(Font.font("System", FontWeight.BOLD, 18));
        resetButton.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8;");
        resetButton.setPrefWidth(220);
        resetButton.setPrefHeight(50);
        resetButton.setOnAction(e -> {
            cancelListening();
            inputController.resetDefaultBindings();
            refreshAllButtons();
        });

        Button backButton = new Button("SAVE & RETURN");
        backButton.setFont(Font.font("System", FontWeight.BOLD, 20));
        backButton.setStyle("-fx-background-color: orange; -fx-text-fill: black; -fx-cursor: hand; -fx-background-radius: 8;");
        backButton.setPrefWidth(260);
        backButton.setPrefHeight(50);
        backButton.setOnAction(e -> exitScene());

        HBox bottomBar = new HBox(25, resetButton, backButton);
        bottomBar.setAlignment(Pos.CENTER);

        this.root.getChildren().addAll(title, grid, bottomBar);
        this.scene = new Scene(root, WIDTH, HEIGHT);

        attachKeyFilter();
    }

    public Scene getScene() {
        return scene;
    }

    /* ========================== KEY CAPTURE & REBINDING ========================== */

    private void attachKeyFilter() {
        // Event filter captures Space/Enter/Arrow keys before focused buttons consume them
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (isListening()) {
                if (event.getCode() != KeyCode.ESCAPE) {
                    inputController.bindKey(listeningAction, event.getCode());
                }
                refreshAllButtons();
                listeningButton = null;
                listeningAction = null;
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                exitScene();
                event.consume();
            }
        });
    }

    private void startListening(Button button, InputAction action) {
        if (listeningButton != null) {
            KeyCode prevKey = inputController.getKeyForAction(listeningAction);
            listeningButton.setText(formatKeyName(prevKey));
            listeningButton.setStyle(BTN_DEFAULT_STYLE);
        }

        listeningButton = button;
        listeningAction = action;
        button.setText("[ PRESS KEY ]");
        button.setStyle(BTN_LISTENING_STYLE);
        root.requestFocus();
    }

    private void cancelListening() {
        if (listeningButton != null) {
            listeningButton = null;
            listeningAction = null;
            refreshAllButtons();
        }
    }

    private void exitScene() {
        cancelListening();
        if (onBackRequested != null) {
            onBackRequested.run();
        }
    }

    private void refreshAllButtons() {
        for (Map.Entry<InputAction, Button> entry : buttonMap.entrySet()) {
            KeyCode currentKey = inputController.getKeyForAction(entry.getKey());
            entry.getValue().setText(formatKeyName(currentKey));
            entry.getValue().setStyle(BTN_DEFAULT_STYLE);
        }
    }

    private String formatKeyName(KeyCode code) {
        return (code != null) ? code.name() : "NONE";
    }

    public boolean isListening() {
        return listeningButton != null && listeningAction != null;
    }
}