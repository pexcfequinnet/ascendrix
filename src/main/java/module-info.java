module org.example.ascendrix {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires javafx.graphics;

    opens org.example.ascendrix to javafx.fxml;
    exports org.example.ascendrix;
}