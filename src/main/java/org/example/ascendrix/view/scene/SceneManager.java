package org.example.ascendrix.view.scene;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.ascendrix.controller.InputController;
import org.example.ascendrix.controller.StorageController;
import org.example.ascendrix.model.mechanics.HandlingConfig;
import org.example.ascendrix.model.mode.GameMode;
import org.example.ascendrix.model.input.KeyBindingConfig;
import org.example.ascendrix.model.progression.ScoreManager;
import org.example.ascendrix.model.state.GameSession;

public class SceneManager {

    private final Stage stage;
    private final StorageController storageController;
    private final ScoreManager scoreManager;
    private final InputController inputController;

    private HandlingConfig playerHandling;

    public SceneManager(Stage stage) {
        this.stage = stage;
        this.storageController = new StorageController();

        this.scoreManager = new ScoreManager();
        this.storageController.loadScores(scoreManager);

        KeyBindingConfig keyBindingConfig = new KeyBindingConfig();
        this.inputController = new InputController(keyBindingConfig, storageController);
    }

    public SceneManager(Stage stage,
                        StorageController storageController,
                        ScoreManager scoreManager,
                        InputController inputController) {
        this.stage = stage;
        this.storageController = storageController;
        this.scoreManager = scoreManager;
        this.inputController = inputController;
    }

    /* ========================== SCENE NAVIGATION ========================== */

    public void setScene(Scene newScene) {
        if (stage != null && newScene != null) {
            stage.setScene(newScene);
        }
    }

    public void showMenu() {
        MenuScene menuScene = new MenuScene(this);
        setScene(menuScene.getScene());
    }

    public void showModeDetail(GameMode mode) {
        ModeDetailScene detailScene = new ModeDetailScene(this, mode);
        setScene(detailScene.getScene());
    }

    public void showKeyConfig() {
        KeyConfigScene keyConfigScene = new KeyConfigScene(this, inputController);
        setScene(keyConfigScene.getScene());
    }

    public void startGame(GameMode mode) {
        inputController.resetState();
        GameScene gameScene = new GameScene(this, inputController, mode, playerHandling);
        setScene(gameScene.getScene());
        gameScene.start();
    }

    public void showGameEnd(GameMode mode, GameSession session) {
        GameSession.GameResult base = session.getRuleset().getResult();
        long elapsedMs = session.getTimer().getElapsedMs();

        GameEndScene endScene = new GameEndScene(
                this,
                scoreManager,
                storageController,
                mode,
                session.isCleared(),
                base.displayText(),
                base.asSortValue(),
                elapsedMs
        );
        setScene(endScene.getScene());
    }

    /* ========================== SHARED CONTROLLERS & CONFIG ========================== */

    public InputController getInputController() {
        return inputController;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public StorageController getStorageController() {
        return storageController;
    }

    public HandlingConfig getPlayerHandling() {
        return playerHandling;
    }

    public void setPlayerHandling(HandlingConfig playerHandling) {
        this.playerHandling = playerHandling;
    }
}