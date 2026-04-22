package org.example.ascendrix;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameRenderer extends Canvas {

    private final int TILE = 30;
    private final int COLS = 10;
    private final int ROWS = 20;

    public GameRenderer() {
        setWidth(COLS * TILE);
        setHeight(ROWS * TILE);
    }

    public void render(int[][] board, Tetromino current) {
        GraphicsContext gc = getGraphicsContext2D();

        // nền
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // board
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (board[y][x] != 0) {
                    gc.setFill(Color.GRAY);
                    gc.fillRect(x * TILE, y * TILE, TILE, TILE);
                }

                gc.setStroke(Color.DARKGRAY);
                gc.strokeRect(x * TILE, y * TILE, TILE, TILE);
            }
        }

        // current piece
        if (current != null) {
            gc.setFill(Color.CYAN);
            for (int[] p : current.shape) {
                int x = current.x + p[0];
                int y = current.y + p[1];
                gc.fillRect(x * TILE, y * TILE, TILE, TILE);
            }
        }
    }
}