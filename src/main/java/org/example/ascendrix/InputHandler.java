package org.example.ascendrix;

import javafx.scene.Scene;

public class InputHandler {

    private GameRenderer game;

    public InputHandler(GameEngine game, Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT -> game.moveLeft();
                case RIGHT -> game.moveRight();
                case DOWN -> game.softDrop();
                case UP -> game.rotate();
                case Z -> game.rotate();
                case X -> game.rotate();
                case A -> game.rotate();
                case SPACE -> game.hardDrop();
            }
        });
    }
}