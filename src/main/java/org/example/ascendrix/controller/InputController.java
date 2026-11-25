package org.example.ascendrix.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import org.example.ascendrix.model.input.InputBuffer;
import org.example.ascendrix.model.input.KeyBindingConfig;
import org.example.ascendrix.model.input.KeyBindingConfig.InputAction;
import org.example.ascendrix.model.mechanics.rotation.RotationDirection;
import org.example.ascendrix.model.state.GameSession;

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
    private boolean rotate180;
    private boolean hardDrop;
    private boolean hold;

    private Scene attachedScene;
    private Window attachedWindow;

    private final EventHandler<KeyEvent> keyPressedFilter = this::handleKeyPressed;
    private final EventHandler<KeyEvent> keyReleasedFilter = this::handleKeyReleased;
    private final EventHandler<KeyEvent> keyTypedBlocker = Event::consume;
    private final ChangeListener<Boolean> focusListener = (obs, wasFocused, isFocused) -> {
        if (!isFocused) {
            resetState(); // Prevents stuck DAS/ARR if Alt+Tab or an OS popup steals focus
        }
    };

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
        detach();
        this.attachedScene = scene;

        // 1. Suppress JavaFX IME composition & character translation on the game scene
        scene.getRoot().setInputMethodRequests(null);
        scene.addEventFilter(KeyEvent.KEY_TYPED, keyTypedBlocker);

        // 2. Capture raw key transitions before Scene focus traversal (Tab/Arrows/Space/Alt)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedFilter);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, keyReleasedFilter);

        // 3. Reset held keys immediately if the window loses focus
        bindWindowFocusGuard(scene.getWindow());
        scene.windowProperty().addListener((obs, oldWin, newWin) -> bindWindowFocusGuard(newWin));

        Platform.runLater(() -> {
            scene.getRoot().requestFocus();
            RawInputHandler.enableForWindow(scene.getWindow());
        });
    }

    public void detach() {
        if (attachedScene != null) {
            attachedScene.removeEventFilter(KeyEvent.KEY_TYPED, keyTypedBlocker);
            attachedScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedFilter);
            attachedScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleasedFilter);
            attachedScene = null;
        }
        if (attachedWindow != null) {
            attachedWindow.focusedProperty().removeListener(focusListener);
            attachedWindow = null;
        }
    }

    private void bindWindowFocusGuard(Window window) {
        if (attachedWindow != null) {
            attachedWindow.focusedProperty().removeListener(focusListener);
        }
        attachedWindow = window;
        if (attachedWindow != null) {
            attachedWindow.focusedProperty().addListener(focusListener);
        }
    }

    private void handleKeyPressed(KeyEvent e) {
        if (session == null || inputBuffer == null) return;

        long nowNs = System.nanoTime();
        KeyCode code = e.getCode();
        handleDebugShortcuts(code);

        InputAction action = bindingConfig.getActionForKey(code.name());
        if (action == null) return;

        // Consume bound game keys so UI focus navigation or mnemonics never trigger
        e.consume();

        switch (action) {
            case LEFT -> {
                if (!left) {
                    left = true;
                    lastWasLeft = true;
                    inputBuffer.enqueueAction(InputAction.LEFT, nowNs);
                }
            }
            case RIGHT -> {
                if (!right) {
                    right = true;
                    lastWasLeft = false;
                    inputBuffer.enqueueAction(InputAction.RIGHT, nowNs);
                }
            }
            case SOFT_DROP -> softDrop = true;

            case ROTATE_CCW -> {
                if (!rotateCCW) {
                    rotateCCW = true;
                    lastRotationHeld = RotationDirection.CCW;
                    inputBuffer.enqueueAction(InputAction.ROTATE_CCW, nowNs);
                }
            }
            case ROTATE_CW -> {
                if (!rotateCW) {
                    rotateCW = true;
                    lastRotationHeld = RotationDirection.CW;
                    inputBuffer.enqueueAction(InputAction.ROTATE_CW, nowNs);
                }
            }
            case ROTATE_180 -> {
                if (!rotate180) {
                    rotate180 = true;
                    lastRotationHeld = RotationDirection.FLIP;
                    inputBuffer.enqueueAction(InputAction.ROTATE_180, nowNs);
                }
            }
            case HOLD -> {
                if (!hold) {
                    hold = true;
                    inputBuffer.bufferHold();
                    inputBuffer.enqueueAction(InputAction.HOLD, nowNs);
                }
            }
            case HARD_DROP -> {
                if (!hardDrop) {
                    hardDrop = true;
                    inputBuffer.enqueueAction(InputAction.HARD_DROP, nowNs);
                }
            }
        }
    }

    private void handleKeyReleased(KeyEvent e) {
        if (inputBuffer == null) return;

        long nowNs = System.nanoTime();
        InputAction action = bindingConfig.getActionForKey(e.getCode().name());
        if (action == null) return;

        e.consume();

        switch (action) {
            case LEFT -> {
                left = false;
                if (!left && !right) inputBuffer.clearDirection();
            }
            case RIGHT -> {
                right = false;
                if (!left && !right) inputBuffer.clearDirection();
            }
            case SOFT_DROP -> softDrop = false;

            case ROTATE_CCW -> {
                rotateCCW = false;
                if (getHeldIRS() == null) inputBuffer.clearRotation();
            }
            case ROTATE_CW -> {
                rotateCW = false;
                if (getHeldIRS() == null) inputBuffer.clearRotation();
            }
            case ROTATE_180 -> {
                rotate180 = false;
                if (getHeldIRS() == null) inputBuffer.clearRotation();
            }
            case HOLD -> {
                hold = false;
                inputBuffer.clearHold();
            }
            case HARD_DROP -> hardDrop = false;
        }
    }

    /* ========================== BUFFER & STATE HELPERS ========================== */

    private void updateDirectionBuffer(long nowNs) {
        if (inputBuffer == null) return;
        int dir = 0;
        if (left && right) {
            dir = lastWasLeft ? -1 : 1;
        } else if (left) {
            dir = -1;
        } else if (right) {
            dir = 1;
        }

        if (dir != 0) {
            inputBuffer.bufferDirection(dir, nowNs);
            if (isPieceControllable()) {
                inputBuffer.markDirectionConsumed();
            }
        } else {
            inputBuffer.clearDirection();
        }
    }

    private void updateRotationBuffer(long nowNs) {
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
        int heldCount = (rotateCW ? 1 : 0) + (rotateCCW ? 1 : 0) + (rotate180 ? 1 : 0);
        if (heldCount == 0) return null;
        if (heldCount > 1 && isRotationHeld(lastRotationHeld)) {
            return lastRotationHeld;
        }
        if (rotate180) return RotationDirection.FLIP;
        if (rotateCW) return RotationDirection.CW;
        if (rotateCCW) return RotationDirection.CCW;
        return null;
    }

    private boolean isRotationHeld(RotationDirection dir) {
        return switch (dir) {
            case CW -> rotateCW;
            case CCW -> rotateCCW;
            case FLIP -> rotate180;
            case NONE -> false;
        };
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
        rotate180 = false;
        hardDrop = false;
        hold = false;
        lastRotationHeld = RotationDirection.NONE;
        if (inputBuffer != null) {
            inputBuffer.clearAll();
        }
    }
}