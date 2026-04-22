package org.example.ascendrix;

import javafx.scene.Scene;

public class InputHandler {

    private GameRenderer game;

    public InputHandler(GameEngine game, Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A -> game.moveLeft();
                case D -> game.moveRight();
                case S -> game.softDrop();
                case W -> game.hardDrop();
                case K -> game.rotateCCW();
                case L -> game.rotateCCW();
                case J -> game.rotateCW();
            }
        });
    }
}