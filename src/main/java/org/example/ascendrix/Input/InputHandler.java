package org.example.ascendrix.Input;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.MainGame.Engine.GamePhase;
import org.example.ascendrix.Rotation.RotationDirection;

import java.io.*;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class InputHandler {
    private static final String BIND_FILE = "GameData/Config/keybinds.json"; // Để chung thư mục dữ liệu cho gọn
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private RotationDirection lastRotationHeld;
    private InputBuffer inputBuffer;
    private BooleanSupplier isRunning;
    private GameEngine game;
    private boolean lastWasLeft = true;

    private boolean left, right, softDrop;
    private boolean rotateCW, rotateCCW, hardDrop, hold;

    private final Map<InputAction, KeyCode> currentBindings = new EnumMap<>(InputAction.class);
    private final Map<KeyCode, InputAction> keyToAction = new HashMap<>();

    public InputHandler() {
        if (!loadKeybinds()) {
            setDefaultBindings();
        }
    }
    public void attachGame(GameEngine game) {
        this.game = game;
        this.inputBuffer = game.getInputBuffer();
        this.isRunning = () -> game.phase == GamePhase.PLAYING || game.isSpawning();
    }

    private void setDefaultBindings() {
        bindKey(InputAction.LEFT, KeyCode.A);
        bindKey(InputAction.RIGHT, KeyCode.D);
        bindKey(InputAction.SOFT_DROP, KeyCode.S);
        bindKey(InputAction.HARD_DROP, KeyCode.W);
        bindKey(InputAction.ROTATE_CCW, KeyCode.J);
        bindKey(InputAction.ROTATE_CW, KeyCode.L);
        bindKey(InputAction.HOLD, KeyCode.SHIFT);
        saveKeybinds(); // Lưu lại file mặc định lần đầu
    }

    public void bindKey(InputAction action, KeyCode newKey) {
        // Hủy liên kết phím cũ của hành động này
        KeyCode oldKey = currentBindings.get(action);
        if (oldKey != null) {
            keyToAction.remove(oldKey);
        }

        // Hủy liên kết nếu phím mới này đang bị trùng ở một hành động khác (Anti-duplicate)
        InputAction conflictAction = keyToAction.get(newKey);
        if (conflictAction != null && conflictAction != action) {
            currentBindings.remove(conflictAction);
        }

        currentBindings.put(action, newKey);
        keyToAction.put(newKey, action);
    }

    // ==========================================
    // 📂 LOGIC ĐỌC / GHI FILE JSON (Bọc lót chống lỗi JPMS)
    // ==========================================
    public void saveKeybinds() {
        try {
            File file = new File(BIND_FILE);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            // Biến đổi Map thành dạng chuỗi thuần String -> String để Gson ko dùng Reflection bậy bạ
            Map<String, String> stringMap = new HashMap<>();
            for (Map.Entry<InputAction, KeyCode> entry : currentBindings.entrySet()) {
                stringMap.put(entry.getKey().name(), entry.getValue().name());
            }

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(stringMap, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    private boolean loadKeybinds() {
        File file = new File(BIND_FILE);
        if (!file.exists() || file.length() == 0) return false;

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> stringMap = gson.fromJson(reader, type);
            if (stringMap == null) return false;

            currentBindings.clear();
            keyToAction.clear();

            for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                InputAction action = InputAction.valueOf(entry.getKey());
                KeyCode code = KeyCode.valueOf(entry.getValue());
                bindKey(action, code);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error reading config. " + e.getMessage());
            return false;
        }
    }

    public KeyCode getKeyForAction(InputAction action) {
        return currentBindings.get(action);
    }

    private void updateDirectionBuffer() {
        if (left && right) {
            game.getInputBuffer().bufferDirection(lastWasLeft ? -1 : 1);
        } else if (left) {
            game.getInputBuffer().bufferDirection(-1);
        } else if (right) {
            game.getInputBuffer().bufferDirection(1);
        } else {
            game.getInputBuffer().clearDirection();
        }
    }

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (!isRunning.getAsBoolean()) return;
            KeyCode code = e.getCode();

            switch (code) {
                case F1 -> game.debugSetLevel(199);
                case F2 -> game.debugSetLevel(499);
                case F3 -> game.debugSetLevel(899);
                case F4 -> game.debugSetLevel(995);
                case F5 -> game.debugSetLevel(1495);
            }

            InputAction action = keyToAction.get(code);
            if (action == null) return;

            switch (action) {
                case LEFT -> {
                    if (!left) {
                        left = true;
                        lastWasLeft = true;
                        updateDirectionBuffer();
                        if (!game.isSpawning()) game.move(-1);
                    }
                }
                case RIGHT -> {
                    if (!right) {
                        right = true;
                        lastWasLeft = false;
                        updateDirectionBuffer();
                        if (!game.isSpawning()) game.move(1);
                    }
                }
                case SOFT_DROP -> softDrop = true;
                case ROTATE_CCW -> {
                    if (!rotateCCW) {
                        rotateCCW = true;
                        lastRotationHeld = RotationDirection.CCW;
                        if (!game.isSpawning()) game.rotateCCW();
                    }
                }
                case ROTATE_CW -> {
                    if (!rotateCW) {
                        rotateCW = true;
                        lastRotationHeld = RotationDirection.CW;
                        if (!game.isSpawning()) game.rotateCW();
                    }
                }
                case HOLD -> {
                    if (!hold) {
                        hold = true;
                        game.getInputBuffer().bufferHold();
                        if (!game.isSpawning()) game.hold();
                    }
                }
                case HARD_DROP -> {
                    if (!hardDrop) {
                        hardDrop = true;
                        game.hardDrop(System.nanoTime());
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            InputAction action = keyToAction.get(e.getCode());
            if (action == null) return;

            switch (action) {
                case LEFT -> { left = false; updateDirectionBuffer(); }
                case RIGHT -> { right = false; updateDirectionBuffer(); }
                case SOFT_DROP -> softDrop = false;
                case ROTATE_CCW -> rotateCCW = false;
                case ROTATE_CW -> rotateCW = false;
                case HOLD -> { hold = false; game.getInputBuffer().clearHold(); }
                case HARD_DROP -> hardDrop = false;
            }
        });

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public RotationDirection getHeldIRS() {
        if (rotateCW && rotateCCW) return lastRotationHeld;
        if (rotateCW) return RotationDirection.CW;
        if (rotateCCW) return RotationDirection.CCW;
        return null;
    }

    public int getHorizontal() {
        if (!isRunning.getAsBoolean()) return 0;
        if (left && right) return lastWasLeft ? -1 : 1;
        if (left) return -1;
        if (right) return 1;
        return 0;
    }

    public boolean isSoftDropHeld() {
        return isRunning.getAsBoolean() && softDrop;
    }
}