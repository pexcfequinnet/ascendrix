package org.example.ascendrix.Input;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import org.example.ascendrix.ARE.LockDelayHandler;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.MainGame.Engine.GamePhase;
import org.example.ascendrix.Rotation.RotationDirection;

import java.util.function.BooleanSupplier;

public class InputHandler {

    private RotationDirection lastRotationHeld;
    private final InputBuffer inputBuffer;
    private final BooleanSupplier isRunning;
    private final GameEngine game;
    private boolean lastWasLeft = true;


    // held states
    private boolean left, right, softDrop;

    // instant (edge-triggered)
    private boolean rotateCW, rotateCCW, hardDrop, hold;


    // direction priority (when holding both)

    private void updateDirectionBuffer() {
        if (left && right) {
            game.getInputBuffer()
                    .bufferDirection(lastWasLeft ? -1 : 1);
        }
        else if (left) {
            game.getInputBuffer().bufferDirection(-1);
        }
        else if (right) {
            game.getInputBuffer().bufferDirection(1);
        }
        else {
            game.getInputBuffer().clearDirection();
        }
    }

    public InputHandler(GameEngine game) {
        this.game = game;
        this.inputBuffer = game.getInputBuffer();
        this.isRunning = () -> game.phase == GamePhase.PLAYING || game.isSpawning();
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
                        updateDirectionBuffer();

                        if (!game.isSpawning()) {
                            game.move(-1);
                        }
                    }
                }
                case D -> {
                    if (!right) {
                        right = true;
                        lastWasLeft = false;
                        updateDirectionBuffer();

                        if (!game.isSpawning()) {
                            game.move(1);
                        }
                    }
                }
                case S -> softDrop = true;

                case J -> {
                    if (!rotateCCW) {
                        rotateCCW = true;
                        lastRotationHeld = RotationDirection.CCW;

                        if (!game.isSpawning()) {
                            game.rotateCCW();
                        }
                    }
                }
                case L -> {
                    if (!rotateCW) {
                        rotateCW = true;
                        lastRotationHeld = RotationDirection.CW;

                        if (!game.isSpawning()) {
                            game.rotateCW();
                        }
                    }
                }
                case SHIFT -> {
                    if (!hold) {
                        hold = true;
                        game.getInputBuffer().bufferHold();
                        if (!game.isSpawning()) game.hold();
                    }
                }

                case W -> {
                    if (!hardDrop) {
                        hardDrop = true;
                        game.hardDrop(System.nanoTime());
                    }
                }

                // DEBUG: REMOVE IF FINISHED TESTING
                case F1 -> game.debugSetLevel(199);  // jump to section 2
                case F2 -> game.debugSetLevel(499);  // jump to section 3
                case F3 -> game.debugSetLevel(899);  // trigger speed level check
                case F4 -> game.debugSetLevel(995);
                case F5 -> game.debugSetLevel(1495);
            }
        });

        scene.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();

            switch (code) {
                case A -> {
                    left = false;
                    updateDirectionBuffer();
                }
                case D -> {
                    right = false;
                    updateDirectionBuffer();}
                case S -> softDrop = false;
                case J     -> rotateCCW = false;
                case L     -> rotateCW  = false;
                case SHIFT -> { hold      = false; game.getInputBuffer().clearHold();      }
                case W -> hardDrop = false;
            }
        });

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    // ===== HELD INPUT for IRS, IHS, DAS, ARR, SD =====
    public boolean isRotateCWHeld()  { return rotateCW; }
    public boolean isRotateCCWHeld() { return rotateCCW; }

    public RotationDirection getHeldIRS() {

        if (rotateCW && rotateCCW) {
            return lastRotationHeld;
        }

        if (rotateCW) {
            return RotationDirection.CW;
        }

        if (rotateCCW) {
            return RotationDirection.CCW;
        }

        return null;
    }
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
}