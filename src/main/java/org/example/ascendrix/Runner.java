package org.example.ascendrix;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Runner extends Application {
    @Override
    public void start(Stage stage) {
        MovementConfig config = new MovementConfig();
        config.dasFrames = 7;   //167ms on 60fps
        //config.arrFrames = 2;   // move left-right once each frame
        //config.sdfFrames = 1;   // move down once each frame
        config.instantArr = true;
        config.instantSdf = true;
        SprintMode mode = new SprintMode(40);
        Ruleset ruleset = new SprintRuleset(
                new DefaultHandling(config),
                new StandardRotationSystem()
        );

        GameRenderer renderer = new GameRenderer();
        GameEngine engine = new GameEngine(renderer, ruleset, mode);
        InputHandler input = new InputHandler(engine::isRunning);
        engine.setInput(input);

        Scene scene = new Scene(new Pane(renderer), 500, 600);

        input.attach(scene);

        stage.setTitle("Tetris test");
        stage.setScene(scene);
        stage.show();
        engine.startCountdown();
        engine.start();
    }
}
