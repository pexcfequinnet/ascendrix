package org.example.ascendrix;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.function.BooleanSupplier;

public class InputHandler {

    private final BooleanSupplier isRunning;
    private final GameEngine game;

    // held states
    private boolean left, right, softDrop;

    // instant (edge-triggered)
    private boolean rotateCW, rotateCCW, hardDrop, hold;

    // previous frame snapshot (for edge detection if needed)
    private boolean prevRotateCW, prevRotateCCW, prevHardDrop, prevHold;

    // direction priority (when holding both)
    private boolean lastWasLeft = true;

    public InputHandler(GameEngine game, BooleanSupplier isRunning) {
        this.game = game;
        this.isRunning = () -> game.phase == GamePhase.PLAYING; // ← check phase directly
    }


    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (!isRunning.getAsBoolean()) return;

            KeyCode code = e.getCode();

            switch (code) {
                case A -> {
                    if (!left) {
                        left = true;
                        lastWasLeft = true;
                        game.move(-1); //
                    }
                }
                case D -> {
                    if (!right) {
                        right = true;
                        lastWasLeft = false;
                        game.move(1);
                    }
                }
                case S -> softDrop = true;

                case J -> {
                    if (!rotateCCW) {
                        rotateCCW = true;

                        if (game.isSpawning()) {
                            game.getInputBuffer().bufferRotateCCW();
                        } else {
                            game.rotateCCW();
                        }
                    }
                }
                case L -> {
                    if (!rotateCW) {
                        rotateCW = true;

                        if (game.isSpawning()) {
                            game.getInputBuffer().bufferRotateCW();
                        } else {
                            game.rotateCW();
                        }
                    }
                }
                case W -> {
                    if (!hardDrop) {
                        hardDrop = true;
                        game.hardDrop(System.nanoTime());
                    }
                }
                case SHIFT -> {
                    if (!hold) {
                        hold = true;

                        if (game.isSpawning()) {
                            game.getInputBuffer().bufferHold(); //IHS
                        } else {
                            game.hold();
                        }
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();

            switch (code) {
                case A -> left = false;
                case D -> right = false;
                case S -> softDrop = false;

                case J -> rotateCCW = false;
                case L -> rotateCW = false;
                case W -> hardDrop = false;
                case SHIFT -> hold = false;
            }
        });

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public void tick() {
        prevRotateCW  = rotateCW;
        prevRotateCCW = rotateCCW;
        prevHardDrop  = hardDrop;
        prevHold      = hold;
    }

    // ===== HELD INPUT (for DAS/ARR) =====

    public int getHorizontal() {
        if (!isRunning.getAsBoolean()) return 0;

        if (left && right) {
            return lastWasLeft ? -1 : 1;
        }
        if (left) return -1;
        if (right) return 1;
        return 0;
    }

    public boolean isSoftDropHeld() {
        return isRunning.getAsBoolean() && softDrop;
    }

    // ===== OPTIONAL EDGE (nếu bạn vẫn cần) =====

    public boolean isRotateCWJustPressed() {
        return isRunning.getAsBoolean() && rotateCW && !prevRotateCW;
    }

    public boolean isRotateCCWJustPressed() {
        return isRunning.getAsBoolean() && rotateCCW && !prevRotateCCW;
    }

    public boolean isHardDropJustPressed() {
        return isRunning.getAsBoolean() && hardDrop && !prevHardDrop;
    }

    public boolean isHoldJustPressed() {
        return isRunning.getAsBoolean() && hold && !prevHold;
    }
}