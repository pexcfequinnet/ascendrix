package org.example.ascendrix;

import javafx.scene.Scene;

import java.util.function.BooleanSupplier;

public class InputHandler {

    private final BooleanSupplier isRunning;
    private GameEngine game;
    private boolean left, right, softDrop;
    private boolean rotateCW, rotateCCW, hardDrop, hold;

    public InputHandler(BooleanSupplier isRunning) {
        this.isRunning = isRunning;
        this.game = game;
    }
    // Previous frame snapshot
    private boolean prevRotateCW, prevRotateCCW, prevHardDrop, prevHold;

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A     -> left      = true;
                case D     -> right     = true;
                case S     -> softDrop  = true;
                case J     -> rotateCCW = true;
                case L     -> rotateCW  = true;
                case W     -> hardDrop  = true;
                case SHIFT -> hold      = true;
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A     -> left      = false;
                case D     -> right     = false;
                case S     -> softDrop  = false;
                case J     -> rotateCCW = false;
                case L     -> rotateCW  = false;
                case W     -> hardDrop  = false;
                case SHIFT -> hold      = false;
            }
        });
    }

    // Call once per frame before reading input
    public void tick() {
        prevRotateCW  = rotateCW;
        prevRotateCCW = rotateCCW;
        prevHardDrop  = hardDrop;
        prevHold      = hold;
    }
    public boolean isLeftHeld() { return isRunning.getAsBoolean() && left; }
    public boolean isRightHeld()      { return isRunning.getAsBoolean() && right; }
    public boolean isSoftDropHeld()   { return isRunning.getAsBoolean() && softDrop; }

    public boolean isRotateCWJustPressed()  { return isRunning.getAsBoolean() && rotateCW  && !prevRotateCW; }
    public boolean isRotateCCWJustPressed() { return isRunning.getAsBoolean() && rotateCCW && !prevRotateCCW; }
    public boolean isHardDropJustPressed()  { return isRunning.getAsBoolean() && hardDrop  && !prevHardDrop; }
    public boolean isHoldJustPressed()      { return isRunning.getAsBoolean() && hold      && !prevHold; }
}