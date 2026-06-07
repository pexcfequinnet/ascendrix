package org.example.ascendrix.UI;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.ascendrix.GameMode.GameMode;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class MenuScene {

    private final Scene scene;
    public MenuScene(SceneManager manager) {

        // 1. ASCII Art "Ascendrix" (Sử dụng font Slant)
        // Lưu ý: Các dấu gạch chéo ngược (\) đã được escape (\\) để code Java không báo lỗi
        String asciiLogo =
"                                                                                                          \n" +
        "                                                       ,--.                                               \n" +
        "   ,---,       .--.--.     ,----..      ,---,.       ,--.'|    ,---,    ,-.----.     ,---, ,--,     ,--,  \n" +
        "  '  .' \\     /  /    '.  /   /   \\   ,'  .' |   ,--,:  : |  .'  .' `\\  \\    /  \\ ,`--.' | |'. \\   / .`|  \n" +
        " /  ;    '.  |  :  /`. / |   :     :,---.'   |,`--.'`|  ' :,---.'     \\ ;   :    \\|   :  : ; \\ `\\ /' / ;  \n" +
        ":  :       \\ ;  |  |--`  .   |  ;. /|   |   .'|   :  :  | ||   |  .`\\  ||   | .\\ ::   |  ' `. \\  /  / .'  \n" +
        ":  |   /\\   \\|  :  ;_    .   ; /--` :   :  |-,:   |   \\ | ::   : |  '  |.   : |: ||   :  |  \\  \\/  / ./   \n" +
        "|  :  ' ;.   :\\  \\    `. ;   | ;    :   |  ;/||   : '  '; ||   ' '  ;  :|   |  \\ :'   '  ;   \\  \\.'  /    \n" +
        "|  |  ;/  \\   \\`----.   \\|   : |    |   :   .''   ' ;.    ;'   | ;  .  ||   : .  /|   |  |    \\  ;  ;     \n" +
        "'  :  | \\  \\ ,'__ \\  \\  |.   | '___ |   |  |-,|   | | \\   ||   | :  |  ';   | |  \\'   :  ;   / \\  \\  \\    \n" +
        "|  |  '  '--' /  /`--'  /'   ; : .'|'   :  ;/|'   : |  ; .''   : | /  ; |   | ;\\  \\   |  '  ;  /\\  \\  \\   \n" +
        "|  :  :      '--'.     / '   | '/  :|   |    \\|   | '`--'  |   | '` ,/  :   ' | \\.'   :  |./__;  \\  ;  \\  \n" +
        "|  | ,'        `--'---'  |   :    / |   :   .''   : |      ;   :  .'    :   : :-' ;   |.' |   : / \\  \\  ; \n" +
        "`--''                     \\   \\ .'  |   | ,'  ;   |.'      |   ,.'      |   |.'   '---'   ;   |/   \\  ' | \n" +
        "                           `---`    `----'    '---'        '---'        `---'             `---'     `--`  \n" +
        "                                                                                                          ";

        Label titleLabel = new Label(asciiLogo);
        titleLabel.setWrapText(false);

        // Dùng font Consolas hoặc Courier New đều rất đẹp với hệ chữ block này
        titleLabel.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #00FFCC; " +
                        "-fx-line-spacing: -1px; " + // Thu hẹp khoảng cách dòng cho các khối khít vào nhau
                        "-fx-alignment: center;"
        );
        // 2. Khởi tạo Buttons
        Button sprintBtn = new Button("SPRINT");
        Button marathonBtn = new Button("MARATHON");
        Button masterBtn = new Button("MASTER");
        Button overdriveBtn = new Button("OVERDRIVE");
        Button btnOptions = new Button("SETTINGS");

        sprintBtn.setOnAction(e -> manager.setScene(new ModeDetailScene(manager, GameMode.SPRINT).scene));
        marathonBtn.setOnAction(e -> manager.setScene(new ModeDetailScene(manager, GameMode.MARATHON).scene));
        masterBtn.setOnAction(e -> manager.setScene(new ModeDetailScene(manager, GameMode.MASTER).scene));
        overdriveBtn.setOnAction(e -> manager.setScene(new ModeDetailScene(manager, GameMode.OVERDRIVE).scene));

        btnOptions.setFont(Font.font("System", FontWeight.BOLD, 16));
        btnOptions.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
        btnOptions.setMaxWidth(200); // Thao tác chỉnh size cho đều với các nút khác của bạn

        // 3. Áp dụng màu sắc riêng cho từng Mode
        // Cú pháp: styleButton(nút, "màu nền", "màu khi di chuột vào");

        // SPRINT:
        styleButton(sprintBtn, "#00c8c8", "#00e1e1");

        // MARATHON:
        styleButton(marathonBtn, "#66BB6A", "#81C784");

        // MASTER:
        styleButton(masterBtn, "#42A5F5", "#64B5F6");

        // OVERDRIVE:
        styleButton(overdriveBtn, "#EF5350", "#E57373");

        //SETTINGS:
        styleButton(btnOptions, "#424242", "#616161");

        btnOptions.setOnAction(e -> {
            var inputHandler = manager.getInputHandler();
            Scene currentMenuScene = btnOptions.getScene();

            KeyConfigMenu configMenu = new KeyConfigMenu(inputHandler, () -> manager.setScene(currentMenuScene));

            Scene optionsScene = new Scene(configMenu, 1024, 768);

            optionsScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, keyEvent -> {
                // Chỉ khi đang ở trạng thái chờ gõ phím mới chặn, tránh ảnh hưởng chức năng khác
                if (configMenu.isListening()) {
                    configMenu.handleSceneKeyEvent(keyEvent);
                }
            });

            manager.setScene(optionsScene);
        });

        // 4. Bố cục VBox
        VBox root = new VBox(15, titleLabel, sprintBtn, marathonBtn, masterBtn, overdriveBtn, btnOptions);
        root.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 30;");
        root.setAlignment(Pos.CENTER);

        scene = new Scene(root, 1024, 768);
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