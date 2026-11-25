module org.example.ascendrix {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.almasb.fxgl.all;
    requires com.google.gson;

    // Entry point (JavaFX Application launcher)
    exports org.example.ascendrix;
    opens org.example.ascendrix to javafx.graphics, javafx.fxml;

    // Model
    exports org.example.ascendrix.model.board;
    exports org.example.ascendrix.model.input;
    exports org.example.ascendrix.model.mechanics;
    exports org.example.ascendrix.model.mechanics.rotation;
    exports org.example.ascendrix.model.mode;
    exports org.example.ascendrix.model.mode.shared;
    exports org.example.ascendrix.model.piece;
    exports org.example.ascendrix.model.progression;
    exports org.example.ascendrix.model.state;

    // Allow Gson to serialize/deserialize ScoreRecord fields and private no-arg constructor
    opens org.example.ascendrix.model.progression to com.google.gson;

    // View
    exports org.example.ascendrix.view.render;
    exports org.example.ascendrix.view.scene;
    opens org.example.ascendrix.view.scene to javafx.fxml;

    // Controller
    exports org.example.ascendrix.controller;
    opens org.example.ascendrix.model.input to com.google.gson;
    opens org.example.ascendrix.view.render to javafx.fxml;
    opens org.example.ascendrix.controller to com.google.gson;
}