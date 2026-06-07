package org.example.ascendrix.UI;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
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

        sprintBtn.setOnAction(e -> manager.setScene(new ModeDetailScene(manager, GameMode.SPRINT).scene));
        marathonBtn.setOnAction(e -> manager.setScene(new ModeDetailScene(manager, GameMode.MARATHON).scene));
        masterBtn.setOnAction(e -> manager.setScene(new ModeDetailScene(manager, GameMode.MASTER).scene));
        overdriveBtn.setOnAction(e -> manager.setScene(new ModeDetailScene(manager, GameMode.OVERDRIVE).scene));

        // 3. Áp dụng màu sắc riêng cho từng Mode
        // Cú pháp: styleButton(nút, "màu nền", "màu khi di chuột vào");

        // SPRINT: Giữ màu xám đen trung tính
        styleButton(sprintBtn, "#333333", "#555555");

        // MARATHON: Xanh lá nhạt (Soft Green)
        styleButton(marathonBtn, "#66BB6A", "#81C784");

        // MASTER: Xanh dương nhạt (Soft Blue)
        styleButton(masterBtn, "#42A5F5", "#64B5F6");

        // OVERDRIVE: Đỏ nhạt (Soft Red - Giảm gắt so với bản cũ)
        styleButton(overdriveBtn, "#EF5350", "#E57373");

        // 4. Bố cục VBox
        VBox root = new VBox(15, titleLabel, sprintBtn, marathonBtn, masterBtn, overdriveBtn);
        root.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 30;");
        root.setAlignment(Pos.CENTER);

        scene = new Scene(root, 850, 600);
    }

    // Hàm Helper giúp set CSS cho Button nhanh gọn hơn
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