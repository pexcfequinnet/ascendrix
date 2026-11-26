package org.example.ascendrix.view.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.ascendrix.model.mechanics.HandlingConfig;
import org.example.ascendrix.model.mode.GameMode;
import org.example.ascendrix.model.progression.ScoreManager;
import org.example.ascendrix.model.progression.ScoreRecord;

import java.util.List;
import java.util.Locale;

public class ModeDetailScene {

    private static final double WIDTH = 1024.0;
    private static final double HEIGHT = 768.0;
    private static final int LEADERBOARD_SLOTS = 10;

    private static final double REFERENCE_FPS = 60.0;
    private static final double STEP_FRAMES = 0.1;

    private static final double MIN_DAS_F = 0.0;
    private static final double MAX_DAS_F = 20.0;
    private static final double DEFAULT_DAS_F = 8.1; // 135 ms

    private static final double MIN_ARR_F = 0.0;
    private static final double MAX_ARR_F = 6.0;
    private static final double DEFAULT_ARR_F = 2.0; // 33.3 ms

    private static final double MIN_SDF_F = 0.0;
    private static final double MAX_SDF_F = 20.0;
    private static final double DEFAULT_SDF_F = 3.0; // 50 ms

    private final Scene scene;

    public ModeDetailScene(SceneManager manager, GameMode mode) {
        String themeColor = mode.getAccentColor();

        // 1. Mode Title
        Label titleLabel = new Label(mode.getDisplayName() + " MODE");
        titleLabel.setStyle(
                "-fx-font-family: 'Monospaced', 'Courier New', monospace; " +
                        "-fx-font-size: 36px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + themeColor + ";"
        );

        // 2. Left Column: Mode Configuration
        VBox configBox = new VBox(14);
        configBox.setAlignment(Pos.TOP_LEFT);
        configBox.setPrefWidth(360);

        Label configTitle = new Label("CONFIGURATION (60 FPS)");
        configTitle.setStyle("-fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-font-size: 16px;");
        configBox.getChildren().add(configTitle);

        if (mode == GameMode.SPRINT) {
            buildSprintHandlingControls(configBox, manager, themeColor);
        } else {
            if (mode == GameMode.MASTER_11 || mode == GameMode.OVERDRIVE) {
                configBox.getChildren().add(createInfoLabel("Start Level: 0"));
            } else if (mode != GameMode.ULTRA) {
                configBox.getChildren().add(createInfoLabel("Start Level: 1"));
            }
            configBox.getChildren().addAll(
                    createInfoLabel("DAS: Auto"),
                    createInfoLabel("ARR: Auto")
            );
        }

        // 3. Right Column: Top 10 High Scores
        VBox scoreBox = new VBox(10);
        scoreBox.setAlignment(Pos.TOP_LEFT);

        Label scoreTitle = new Label("HIGH SCORES");
        scoreTitle.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 16px;");
        scoreBox.getChildren().add(scoreTitle);

        ScoreManager scoreManager = manager.getScoreManager();
        List<ScoreRecord> topScores = scoreManager.getTopScores(mode.name());

        if (topScores.isEmpty()) {
            scoreBox.getChildren().addAll(
                    createInfoLabel("No record."),
                    createInfoLabel("Become the first person to set a score")
            );
        } else {
            for (int i = 0; i < LEADERBOARD_SLOTS; i++) {
                if (i < topScores.size()) {
                    ScoreRecord record = topScores.get(i);
                    String displayText = String.format("%2d. %s - %s", (i + 1), record.getDisplayValue(), record.getPlayerName());
                    scoreBox.getChildren().add(createInfoLabel(displayText));
                } else {
                    scoreBox.getChildren().add(createInfoLabel(String.format("%2d. ---", (i + 1))));
                }
            }
        }

        HBox centerContent = new HBox(70, configBox, scoreBox);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(25, 0, 25, 0));

        // 4. Action Buttons
        Button playBtn = new Button("START GAME");
        Button backBtn = new Button("BACK TO MENU");

        styleActionBtn(playBtn, themeColor, "#FFFFFF");
        styleActionBtn(backBtn, "#555555", "#CCCCCC");

        playBtn.setOnAction(e -> manager.startGame(mode));
        backBtn.setOnAction(e -> manager.showMenu());

        // 5. Root Layout & Scene
        VBox root = new VBox(20, titleLabel, centerContent, playBtn, backBtn);
        root.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 40;");
        root.setAlignment(Pos.CENTER);

        this.scene = new Scene(root, WIDTH, HEIGHT);

        this.scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                manager.showMenu();
                event.consume();
            }
        });
    }

    /* ========================== SPRINT FRAME CONTROLS ========================== */

    private void buildSprintHandlingControls(VBox configBox, SceneManager manager, String accentColor) {
        HandlingConfig current = manager.getPlayerHandling();

        double initialDasF = clampAndRound(nsToFrames(current.getDasNs()), MIN_DAS_F, MAX_DAS_F);
        double initialArrF = current.isInstantArr()
                ? 0.0
                : clampAndRound(nsToFrames(current.getArrNs()), MIN_ARR_F, MAX_ARR_F);
        double initialSdfF = current.isInstantSdf()
                ? 0.0
                : clampAndRound(nsToFrames(current.getSdfNs()), MIN_SDF_F, MAX_SDF_F);

        Label goalLabel = createInfoLabel("Target: 40 Lines");

        Label dasValueLabel = createValueLabel(formatFrameValue(initialDasF, false), accentColor);
        Slider dasSlider = createFrameSlider(MIN_DAS_F, MAX_DAS_F, initialDasF, 5.0, accentColor);

        Label arrValueLabel = createValueLabel(formatFrameValue(initialArrF, true), accentColor);
        Slider arrSlider = createFrameSlider(MIN_ARR_F, MAX_ARR_F, initialArrF, 1.0, accentColor);

        Label sdfValueLabel = createValueLabel(formatFrameValue(initialSdfF, true), accentColor);
        Slider sdfSlider = createFrameSlider(MIN_SDF_F, MAX_SDF_F, initialSdfF, 5.0, accentColor);

        boolean[] syncing = {false};

        Runnable syncConfig = () -> {
            if (syncing[0]) return;
            syncing[0] = true;
            try {
                double dasF = clampAndRound(dasSlider.getValue(), MIN_DAS_F, MAX_DAS_F);
                double arrF = clampAndRound(arrSlider.getValue(), MIN_ARR_F, MAX_ARR_F);
                double sdfF = clampAndRound(sdfSlider.getValue(), MIN_SDF_F, MAX_SDF_F);

                dasSlider.setValue(dasF);
                arrSlider.setValue(arrF);
                sdfSlider.setValue(sdfF);

                dasValueLabel.setText(formatFrameValue(dasF, false));
                arrValueLabel.setText(formatFrameValue(arrF, true));
                sdfValueLabel.setText(formatFrameValue(sdfF, true));

                boolean instantArr = (arrF <= 0.0);
                boolean instantSdf = (sdfF <= 0.0);

                HandlingConfig updated = new HandlingConfig(
                        framesToNs(dasF),
                        instantArr ? 0L : framesToNs(arrF),
                        instantSdf ? 0L : framesToNs(sdfF),
                        instantArr,
                        instantSdf
                );
                manager.updatePlayerHandling(updated);
            } finally {
                syncing[0] = false;
            }
        };

        dasSlider.valueProperty().addListener((obs, oldVal, newVal) -> syncConfig.run());
        arrSlider.valueProperty().addListener((obs, oldVal, newVal) -> syncConfig.run());
        sdfSlider.valueProperty().addListener((obs, oldVal, newVal) -> syncConfig.run());

        VBox dasRow = createHandlingRow("DAS", dasValueLabel, dasSlider, MIN_DAS_F, MAX_DAS_F, syncConfig);
        VBox arrRow = createHandlingRow("ARR", arrValueLabel, arrSlider, MIN_ARR_F, MAX_ARR_F, syncConfig);
        VBox sdfRow = createHandlingRow("SDF", sdfValueLabel, sdfSlider, MIN_SDF_F, MAX_SDF_F, syncConfig);

        Button resetBtn = new Button("RESET DEFAULTS");
        resetBtn.setStyle(
                "-fx-background-color: #2A2A2A; " +
                        "-fx-text-fill: #CCCCCC; " +
                        "-fx-font-family: 'Monospaced', 'Courier New', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 6 14 6 14; " +
                        "-fx-border-color: #555555; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
        );
        resetBtn.setOnAction(e -> {
            syncing[0] = true;
            try {
                dasSlider.setValue(DEFAULT_DAS_F);
                arrSlider.setValue(DEFAULT_ARR_F);
                sdfSlider.setValue(DEFAULT_SDF_F);
            } finally {
                syncing[0] = false;
            }
            syncConfig.run();
        });

        configBox.getChildren().addAll(goalLabel, dasRow, arrRow, sdfRow, resetBtn);
    }

    private VBox createHandlingRow(String name,
                                   Label valueLabel,
                                   Slider slider,
                                   double min,
                                   double max,
                                   Runnable syncConfig) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle(
                "-fx-text-fill: #E0E0E0; " +
                        "-fx-font-family: 'Monospaced', 'Courier New', monospace; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(nameLabel, spacer, valueLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefWidth(340);

        Button minusBtn = createStepButton("-");
        minusBtn.setOnAction(e -> {
            slider.setValue(clampAndRound(slider.getValue() - STEP_FRAMES, min, max));
            syncConfig.run();
        });

        Button plusBtn = createStepButton("+");
        plusBtn.setOnAction(e -> {
            slider.setValue(clampAndRound(slider.getValue() + STEP_FRAMES, min, max));
            syncConfig.run();
        });

        HBox controlBar = new HBox(8, minusBtn, slider, plusBtn);
        controlBar.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(6, header, controlBar);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle(
                "-fx-background-color: #242424; " +
                        "-fx-border-color: #383838; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6;"
        );
        return card;
    }

    private Slider createFrameSlider(double min, double max, double value, double majorTick, String accentColor) {
        Slider slider = new Slider(min, max, value);
        slider.setPrefWidth(250);
        slider.setBlockIncrement(STEP_FRAMES);
        slider.setMajorTickUnit(majorTick);
        slider.setMinorTickCount(0);
        slider.setShowTickMarks(true);
        slider.setStyle("-fx-accent: " + accentColor + "; -fx-cursor: hand;");
        return slider;
    }

    private Button createStepButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(28, 26);
        btn.setMinSize(28, 26);
        String idle =
                "-fx-background-color: #333333; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-family: 'Monospaced', 'Courier New', monospace; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 0; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;";
        String hover =
                "-fx-background-color: #4A4A4A; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-family: 'Monospaced', 'Courier New', monospace; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 0; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;";
        btn.setStyle(idle);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(idle));
        return btn;
    }

    private Label createValueLabel(String text, String accentColor) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-text-fill: " + accentColor + "; " +
                        "-fx-font-family: 'Monospaced', 'Courier New', monospace; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        );
        return lbl;
    }

    private String formatFrameValue(double frames, boolean zeroIsInstant) {
        if (zeroIsInstant && frames <= 0.0) {
            return "0.0F (INSTANT)";
        }
        long ms = Math.round(frames * 1000.0 / REFERENCE_FPS);
        return String.format(Locale.US, "%.1fF (%d ms)", frames, ms);
    }

    private double clampAndRound(double value, double min, double max) {
        double rounded = Math.round(value * 10.0) / 10.0;
        return Math.clamp(rounded, min, max);
    }

    private double nsToFrames(long ns) {
        return (ns * REFERENCE_FPS) / 1_000_000_000.0;
    }

    private long framesToNs(double frames) {
        return Math.round(frames * 1_000_000_000.0 / REFERENCE_FPS);
    }

    /* ========================== HELPERS ========================== */

    private Label createInfoLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-family: 'Monospaced', 'Courier New', monospace;");
        return lbl;
    }

    private void styleActionBtn(Button btn, String bgColor, String textColor) {
        String baseStyle =
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-pref-width: 250px; " +
                        "-fx-pref-height: 45px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-cursor: hand; ";

        String idleStyle = baseStyle + "-fx-background-color: " + bgColor + ";";
        String hoverStyle = baseStyle + "-fx-background-color: derive(" + bgColor + ", 20%);";

        btn.setStyle(idleStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(idleStyle));
    }

    public Scene getScene() {
        return scene;
    }
}