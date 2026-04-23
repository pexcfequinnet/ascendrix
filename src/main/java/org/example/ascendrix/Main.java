package org.example.ascendrix;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        GameRenderer renderer = new GameRenderer();
        GameEngine engine = new GameEngine(renderer);

        Scene scene = new Scene(new Pane(renderer), 525, 600);
        new InputHandler(engine, scene);

        stage.setTitle("Tetris test");
        stage.setScene(scene);
        stage.show();
        engine.start(); // start GameLoop
    }
}
