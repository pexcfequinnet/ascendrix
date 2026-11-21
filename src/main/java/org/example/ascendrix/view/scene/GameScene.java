package org.example.ascendrix.view.scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.ascendrix.controller.GameLoopController;
import org.example.ascendrix.model.input.InputBuffer;
import org.example.ascendrix.controller.InputController;
import org.example.ascendrix.model.mechanics.HandlingConfig;
import org.example.ascendrix.model.mode.GameMode;
import org.example.ascendrix.model.mode.ModeFactory;
import org.example.ascendrix.model.mode.ModeRuleset;
import org.example.ascendrix.model.state.GamePhase;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.view.render.GameRenderer;

public class GameScene {

    private static final double WIDTH = 1024.0;
    private static final double HEIGHT = 768.0;

    private final SceneManager sceneManager;
    private final InputController inputController;
    private final GameMode mode;
    private final HandlingConfig playerHandling;

    private final GameSession session;
    private final GameRenderer renderer;
    private final GameLoopController loopController;

    private final Scene scene;
    private final VBox pauseOverlay;

    public GameScene(SceneManager sceneManager, InputController inputController, GameMode mode) {
        this(sceneManager, inputController, mode, null);
    }

    public GameScene(SceneManager sceneManager,
                     InputController inputController,
                     GameMode mode,
                     HandlingConfig playerHandling) {
        this.sceneManager = sceneManager;
        this.inputController = inputController;
        this.mode = mode;
        this.playerHandling = playerHandling;

        // 1. Build Model & Controller pipeline
        InputBuffer inputBuffer = new InputBuffer();
        ModeRuleset ruleset = ModeFactory.create(mode, playerHandling);
        this.session = new GameSession(mode, ruleset, inputBuffer);
        this.renderer = new GameRenderer();
        this.loopController = new GameLoopController(session, renderer, inputController);

        // 2. Build Root Layout & Pause Overlay
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");

        this.pauseOverlay = createPauseOverlay();
        root.getChildren().addAll(renderer, pauseOverlay);

        this.scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);

        // 3. Attach Gameplay Input & Scene-level Hotkeys (ESC / ENTER)
        inputController.attachSession(session, inputBuffer);
        inputController.attach(scene);
        attachSceneHotkeys();

        // Auto-pause if window loses focus during active play
        scene.windowProperty().addListener((obs, oldWin, newWin) -> {
            if (newWin != null) {
                newWin.focusedProperty().addListener((fObs, wasFocused, isFocused) -> {
                    if (!isFocused && session.getPhase().isPlayable()) {
                        pauseGame();
                    }
                });
            }
        });
    }

    public void start() {
        loopController.start();
    }

    public Scene getScene() {
        return scene;
    }

    /* ========================== SCENE HOTKEYS ========================== */

    private void attachSceneHotkeys() {
        // Use an event filter so ESC / ENTER do not overwrite InputController's setOnKeyPressed handler
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            GamePhase phase = session.getPhase();

            if (code == KeyCode.ESCAPE) {
                if (phase == GamePhase.PAUSED) {
                    resumeGame();
                } else if (phase == GamePhase.COUNTDOWN || phase.isPlayable()) {
                    pauseGame();
                }
                e.consume();
            } else if (code == KeyCode.ENTER && phase.isTerminal()) {
                finishAndGoToEndScreen();
                e.consume();
            }
        });
    }

    /* ========================== PAUSE / NAVIGATION ========================== */

    private void pauseGame() {
        loopController.pause();
        pauseOverlay.setVisible(true);
        pauseOverlay.toFront();
    }

    private void resumeGame() {
        pauseOverlay.setVisible(false);
        loopController.resume();
        scene.getRoot().requestFocus();
    }

    private void restartGame() {
        loopController.stop();
        inputController.resetState();
        sceneManager.startGame(mode);
    }

    private void returnToMenu() {
        loopController.stop();
        inputController.resetState();
        sceneManager.showMenu();
    }

    private void finishAndGoToEndScreen() {
        loopController.stop();
        inputController.resetState();
        sceneManager.showGameEnd(mode, session);
    }

    /* ========================== PAUSE OVERLAY UI ========================== */

    private VBox createPauseOverlay() {
        VBox overlay = new VBox(18);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.78);");
        overlay.setVisible(false);

        Label title = new Label("PAUSED");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 48));

        Button continueBtn = createMenuButton("CONTINUE");
        continueBtn.setOnAction(e -> resumeGame());

        Button restartBtn = createMenuButton("RESTART");
        restartBtn.setOnAction(e -> restartGame());

        Button menuBtn = createMenuButton("MAIN MENU");
        menuBtn.setOnAction(e -> returnToMenu());

        overlay.getChildren().addAll(title, continueBtn, restartBtn, menuBtn);
        return overlay;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(44);
        btn.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        btn.setStyle(
                "-fx-background-color: #1e1e2e;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #00d4ff;" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        );
        return btn;
    }
}