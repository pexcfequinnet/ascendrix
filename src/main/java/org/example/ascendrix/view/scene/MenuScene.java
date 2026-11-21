package org.example.ascendrix.view.scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.example.ascendrix.model.mode.GameMode;

public class MenuScene {

    private static final double WIDTH = 1024.0;
    private static final double HEIGHT = 768.0;

    private final Scene scene;

    public MenuScene(SceneManager manager) {

        // 1. ASCII Art "Ascendrix" (Slant font, fixed 106-column width)
        String asciiLogo = String.join("\n",
                "                                                       ,--.                                               ",
                "   ,---,       .--.--.     ,----..      ,---,.       ,--.'|    ,---,    ,-.----.     ,---, ,--,     ,--,  ",
                "  '  .' \\     /  /    '.  /   /   \\   ,'  .' |   ,--,:  : |  .'  .' `\\  \\    /  \\ ,`--.' | |'. \\   / .`|  ",
                " /  ;    '.  |  :  /`. / |   :     :,---.'   |,`--.'`|  ' :,---.'     \\ ;   :    \\|   :  : ; \\ `\\ /' / ;  ",
                ":  :       \\ ;  |  |--`  .   |  ;. /|   |   .'|   :  :  | ||   |  .`\\  ||   | .\\ ::   |  ' `. \\  /  / .'  ",
                ":  |   /\\   \\|  :  ;_    .   ; /--` :   :  |-,:   |   \\ | ::   : |  '  |.   : |: ||   :  |  \\  \\/  / ./   ",
                "|  :  ' ;.   :\\  \\    `. ;   | ;    :   |  ;/||   : '  '; ||   ' '  ;  :|   |  \\ :'   '  ;   \\  \\.'  /    ",
                "|  |  ;/  \\   \\`----.   \\|   : |    |   :   .''   ' ;.    ;'   | ;  .  ||   : .  /|   |  |    \\  ;  ;     ",
                "'  :  | \\  \\ ,'__ \\  \\  |.   | '___ |   |  |-,|   | | \\   ||   | :  |  ';   | |  \\'   :  ;   / \\  \\  \\    ",
                "|  |  '  '--' /  /`--'  /'   ; : .'|'   :  ;/|'   : |  ; .''   : | /  ; |   | ;\\  \\   |  '  ;  /\\  \\  \\   ",
                "|  :  :      '--'.     / '   | '/  :|   |    \\|   | '`--'  |   | '` ,/  :   ' | \\.'   :  |./__;  \\  ;  \\  ",
                "|  | ,'        `--'---'  |   :    / |   :   .''   : |      ;   :  .'    :   : :-' ;   |.' |   : / \\  \\  ; ",
                "`--''                     \\   \\ .'  |   | ,'  ;   |.'      |   ,.'      |   |.'   '---'   ;   |/   \\  ' | ",
                "                           `---`    `----'    '---'        '---'        `---'             `---'     `--`  "
        );

        // Use Text + Java's logical "Monospaced" font to prevent Linux fallback width overflow & ellipsis clipping
        Text titleText = new Text(asciiLogo);
        titleText.setFont(Font.font("Monospaced", FontWeight.BOLD, 11.5));
        titleText.setFill(Color.web("#00FFCC"));
        titleText.setTextAlignment(TextAlignment.LEFT);
        titleText.setLineSpacing(0);

        // 2. Mode & Settings Buttons
        Button sprintBtn = new Button("SPRINT");
        Button marathonBtn = new Button("MARATHON");
        Button masterBtn = new Button("MASTER");
        Button overdriveBtn = new Button("OVERDRIVE");
        Button settingsBtn = new Button("SETTINGS");

        sprintBtn.setOnAction(e -> manager.showModeDetail(GameMode.SPRINT));
        marathonBtn.setOnAction(e -> manager.showModeDetail(GameMode.MARATHON));
        masterBtn.setOnAction(e -> manager.showModeDetail(GameMode.MASTER));
        overdriveBtn.setOnAction(e -> manager.showModeDetail(GameMode.OVERDRIVE));

        styleButton(sprintBtn, "#00c8c8", "#00e1e1");
        styleButton(marathonBtn, "#66BB6A", "#81C784");
        styleButton(masterBtn, "#42A5F5", "#64B5F6");
        styleButton(overdriveBtn, "#EF5350", "#E57373");
        styleButton(settingsBtn, "#424242", "#616161");



        // 3. Layout
        VBox root = new VBox(15, titleText, sprintBtn, marathonBtn, masterBtn, overdriveBtn, settingsBtn);
        root.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 30;");
        root.setAlignment(Pos.CENTER);

        this.scene = new Scene(root, WIDTH, HEIGHT);

        settingsBtn.setOnAction(e -> {
            KeyConfigScene keyConfigScene = new KeyConfigScene(
                    manager.getInputController(),
                    () -> manager.setScene(scene)
            );
            manager.setScene(keyConfigScene.getScene());
        });
    }

    private void styleButton(Button btn, String bgColor, String hoverColor) {
        String baseStyle =
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white; " +
                        "-fx-pref-width: 220px; " +
                        "-fx-pref-height: 45px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; ";

        String idleStyle = baseStyle + "-fx-background-color: " + bgColor + ";";
        String hoverStyle = baseStyle + "-fx-background-color: " + hoverColor + ";";

        btn.setStyle(idleStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(idleStyle));
    }

    public Scene getScene() {
        return scene;
    }
}