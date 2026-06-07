package org.example.ascendrix.UI;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.example.ascendrix.GameMode.GameMode;

public class MenuScene {

    private final Scene scene;

    public MenuScene(SceneManager manager) {

        Button sprintBtn = new Button("Sprint");
        Button marathonBtn = new Button("Marathon");
        Button masterBtn = new Button("Master");
        Button overdriveBtn = new Button("Overdrive");
        sprintBtn.setOnAction(e -> manager.startGame(GameMode.SPRINT));
        marathonBtn.setOnAction(e -> manager.startGame(GameMode.MARATHON));
        masterBtn.setOnAction(e -> manager.startGame(GameMode.MASTER));
        overdriveBtn.setOnAction(e -> manager.startGame(GameMode.OVERDRIVE));
        VBox root = new VBox(10, sprintBtn, marathonBtn, masterBtn, overdriveBtn);;
        root.setStyle("-fx-alignment: center; -fx-padding: 20;");

        scene = new Scene(root, 400, 600);
    }

    public Scene getScene() {
        return scene;
    }
}