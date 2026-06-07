module org.example.ascendrix {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires javafx.graphics;
    requires com.google.gson;
    opens org.example.ascendrix.GameData to com.google.gson;
    opens org.example.ascendrix to javafx.fxml;
    exports org.example.ascendrix;
    exports org.example.ascendrix.Input;
    opens org.example.ascendrix.Input to javafx.fxml;
    exports org.example.ascendrix.Rotation;
    opens org.example.ascendrix.Rotation to javafx.fxml;
    exports org.example.ascendrix.MainGame;
    opens org.example.ascendrix.MainGame to javafx.fxml;
    exports org.example.ascendrix.Tetromino;
    opens org.example.ascendrix.Tetromino to javafx.fxml;
    exports org.example.ascendrix.GameMode.Master;
    opens org.example.ascendrix.GameMode.Master to javafx.fxml;
    exports org.example.ascendrix.GameMode.Marathon;
    opens org.example.ascendrix.GameMode.Marathon to javafx.fxml;
    exports org.example.ascendrix.GameMode.Sprint;
    opens org.example.ascendrix.GameMode.Sprint to javafx.fxml;
    exports org.example.ascendrix.Movement;
    opens org.example.ascendrix.Movement to javafx.fxml;
    exports org.example.ascendrix.Rotation.SRS;
    opens org.example.ascendrix.Rotation.SRS to javafx.fxml;
    exports org.example.ascendrix.MainGame.Engine;
    opens org.example.ascendrix.MainGame.Engine to javafx.fxml;
    exports org.example.ascendrix.MainGame.Renderer;
    opens org.example.ascendrix.MainGame.Renderer to javafx.fxml;
    exports org.example.ascendrix.UI;
    opens org.example.ascendrix.UI to javafx.fxml;
    exports org.example.ascendrix.MainGame.Ruleset;
    opens org.example.ascendrix.MainGame.Ruleset to javafx.fxml;
    exports org.example.ascendrix.GameMode;
    opens org.example.ascendrix.GameMode to javafx.fxml;
    exports org.example.ascendrix.ARE;
    opens org.example.ascendrix.ARE to javafx.fxml;

}