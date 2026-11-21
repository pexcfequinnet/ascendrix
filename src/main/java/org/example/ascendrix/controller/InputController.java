package org.example.ascendrix.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import org.example.ascendrix.model.input.InputBuffer;
import org.example.ascendrix.model.mechanics.rotation.RotationDirection;
import org.example.ascendrix.model.input.KeyBindingConfig;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.input.KeyBindingConfig.InputAction;

public class InputController {

    private final KeyBindingConfig bindingConfig;
    private final StorageController storageController;

    private GameSession session;
    private InputBuffer inputBuffer;

    private boolean lastWasLeft = true;
    private RotationDirection lastRotationHeld = RotationDirection.NONE;

    private boolean left;
    private boolean right;
    private boolean softDrop;
    private boolean rotateCW;
    private boolean rotateCCW;
    private boolean hardDrop;
    private boolean hold;

    public InputController(KeyBindingConfig bindingConfig, StorageController storageController) {
        this.bindingConfig = bindingConfig;
        this.storageController = storageController;

        if (!storageController.loadKeybinds(bindingConfig)) {
            bindingConfig.resetToDefaults();
            storageController.saveKeybinds(bindingConfig);
        }
    }

    public void attachSession(GameSession session, InputBuffer inputBuffer) {
        this.session = session;
        this.inputBuffer = inputBuffer;
        resetState();
    }

    public void bindKey(InputAction action, KeyCode newKey) {
        if (action == null || newKey == null) return;
        bindingConfig.bind(action, newKey.name());
        storageController.saveKeybinds(bindingConfig);
    }

    public KeyCode getKeyForAction(InputAction action) {
        String keyName = bindingConfig.getKeyName(action);
        if (keyName == null) return null;
        try {
            return KeyCode.valueOf(keyName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void resetDefaultBindings() {
        bindingConfig.resetToDefaults();
        storageController.saveKeybinds(bindingConfig);
    }

    /* ========================== SCENE LISTENER BINDING ========================== */

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (session == null || inputBuffer == null) return;

            KeyCode code = e.getCode();
            handleDebugShortcuts(code);

            InputAction action = bindingConfig.getActionForKey(code.name());
            if (action == null) return;

            boolean canActImmediately = isPieceControllable();

            switch (action) {
                case LEFT -> {
                    if (!left) {
                        left = true;
                        lastWasLeft = true;
                        updateDirectionBuffer();
                        if (canActImmediately) session.move(-1);
                    }
                }
                case RIGHT -> {
                    if (!right) {
                        right = true;
                        lastWasLeft = false;
                        updateDirectionBuffer();
                        if (canActImmediately) session.move(1);
                    }
                }
                case SOFT_DROP -> softDrop = true;

                case ROTATE_CCW -> {
                    if (!rotateCCW) {
                        rotateCCW = true;
                        lastRotationHeld = RotationDirection.CCW;
                        updateRotationBuffer();
                        if (canActImmediately) {
                            session.rotateCCW();
                            inputBuffer.markRotationConsumed();
                        }
                    }
                }
                case ROTATE_CW -> {
                    if (!rotateCW) {
                        rotateCW = true;
                        lastRotationHeld = RotationDirection.CW;
                        updateRotationBuffer();
                        if (canActImmediately) {
                            session.rotateCW();
                            inputBuffer.markRotationConsumed();
                        }
                    }
                }
                case HOLD -> {
                    if (!hold) {
                        hold = true;
                        inputBuffer.bufferHold();
                        if (canActImmediately) session.hold();
                    }
                }
                case HARD_DROP -> {
                    if (!hardDrop) {
                        hardDrop = true;
                        if (canActImmediately) session.hardDrop(System.nanoTime());
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (inputBuffer == null) return;

            InputAction action = bindingConfig.getActionForKey(e.getCode().name());
            if (action == null) return;

            switch (action) {
                case LEFT -> {
                    left = false;
                    updateDirectionBuffer();
                }
                case RIGHT -> {
                    right = false;
                    updateDirectionBuffer();
                }
                case SOFT_DROP -> softDrop = false;

                case ROTATE_CCW -> {
                    rotateCCW = false;
                    updateRotationBuffer();
                }
                case ROTATE_CW -> {
                    rotateCW = false;
                    updateRotationBuffer();
                }
                case HOLD -> {
                    hold = false;
                    inputBuffer.clearHold();
                }
                case HARD_DROP -> hardDrop = false;
            }
        });

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    /* ========================== BUFFER & STATE HELPERS ========================== */

    private void updateDirectionBuffer() {
        if (inputBuffer == null) return;
        if (left && right) {
            inputBuffer.bufferDirection(lastWasLeft ? -1 : 1);
        } else if (left) {
            inputBuffer.bufferDirection(-1);
        } else if (right) {
            inputBuffer.bufferDirection(1);
        } else {
            inputBuffer.clearDirection();
        }
    }

    private void updateRotationBuffer() {
        if (inputBuffer == null) return;
        RotationDirection held = getHeldIRS();
        inputBuffer.bufferRotation(held != null ? held : RotationDirection.NONE);
        if (held != null && isPieceControllable()) {
            inputBuffer.markRotationConsumed();
        }
    }

    private void handleDebugShortcuts(KeyCode code) {
        if (session.getRuleset() == null) return;
        switch (code) {
            case F1 -> session.getRuleset().debugSetLevel(199);
            case F2 -> session.getRuleset().debugSetLevel(499);
            case F3 -> session.getRuleset().debugSetLevel(899);
            case F4 -> session.getRuleset().debugSetLevel(995);
            case F5 -> session.getRuleset().debugSetLevel(1495);
            default -> {}
        }
    }

    private boolean isSessionActive() {
        return session != null && (session.getPhase().isPlayable() || session.isSpawning());
    }

    private boolean isPieceControllable() {
        return session != null
                && session.getPhase().isPlayable()
                && !session.isSpawning()
                && !session.isWaitingForClearAnim();
    }

    public RotationDirection getHeldIRS() {
        if (rotateCW && rotateCCW) return lastRotationHeld;
        if (rotateCW) return RotationDirection.CW;
        if (rotateCCW) return RotationDirection.CCW;
        return null;
    }

    public int getHorizontal() {
        if (!isSessionActive()) return 0;
        if (left && right) return lastWasLeft ? -1 : 1;
        if (left) return -1;
        if (right) return 1;
        return 0;
    }

    public boolean isSoftDropHeld() {
        return isSessionActive() && softDrop;
    }

    public void resetState() {
        left = false;
        right = false;
        softDrop = false;
        rotateCW = false;
        rotateCCW = false;
        hardDrop = false;
        hold = false;
        lastRotationHeld = RotationDirection.NONE;
        if (inputBuffer != null) {
            inputBuffer.clearAll();
        }
    }
}