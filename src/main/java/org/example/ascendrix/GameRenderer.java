package org.example.ascendrix;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

public class GameRenderer extends Canvas {

    private final int TILE = 30;
    private final int COLS = 10;
    private final int ROWS = 20;

    public GameRenderer() {
        setWidth(COLS * TILE + 150);
        setHeight(ROWS * TILE);
    }

    public void render(int[][] board, TetrominoHandler current) {
        GraphicsContext gc = getGraphicsContext2D();
        // background
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
    // Render next pieces
    public void renderNext(List<TetrominoType> preview) {
        GraphicsContext gc = getGraphicsContext2D();

        int baseX = 320;
        int baseY = 50;
        int TILE = 20;

        gc.fillText("NEXT", baseX, baseY - 10);

        for (int i = 0; i < preview.size(); i++) {
            TetrominoType type = preview.get(i);

            int offsetY = baseY + i * 80;

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;

            for (int[] p : type.shape) {
                minX = Math.min(minX, p[0]);
                minY = Math.min(minY, p[1]);
            }

            for (int[] p : type.shape) {
                int x = baseX + (p[0] - minX) * TILE;
                int y = offsetY + (p[1] - minY) * TILE;

                gc.fillRect(x, y, TILE, TILE);
            }
        }
    }
}