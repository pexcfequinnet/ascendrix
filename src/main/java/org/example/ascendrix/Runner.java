package org.example.ascendrix;

import javafx.application.Application;
import javafx.stage.Stage;

public class Runner extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager manager = new SceneManager(stage);
        manager.showMenu();
        stage.setTitle("Ascendrix");
        stage.show();
    }
}
