package org.example.ascendrix;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;


public class GameRenderer extends Canvas {

    private final int TILE = 30;
    private final int COLS = 10;
    private final int ROWS = 20;
    private final int OFFSET_X = 100; // Move entire board to the right to make space for hold and other stats

    public GameRenderer() {
        setWidth(COLS * TILE  + 200);
        setHeight(ROWS * TILE);
    }

    public void renderBackground() {
        GraphicsContext gc = getGraphicsContext2D();
        // Background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

    }
    public void renderCountdown(GamePhase phase, int countdown){
        GraphicsContext g = getGraphicsContext2D();

        if (phase == GamePhase.COUNTDOWN) {

            String text;

            if (countdown > 1) {
                text = String.valueOf(countdown);
            } else if (countdown == 1) {
                text = "READY";
            } else {
                text = "GO";
            }

            g.fillText(text, 200, 300);
        }
    }
    public void renderBoardOverlay() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.color(0.5, 0.5, 0.5, 0.8)); // grey with ~60% opacity
        gc.fillRect(OFFSET_X, 0, COLS * TILE, ROWS * TILE);
    }

    public void renderHUD(GameModeHandler modeHandler, GameTimer timer){
        GraphicsContext gc = getGraphicsContext2D();
        if (modeHandler != null) {
            modeHandler.renderHUD(gc, timer);
        }
    }
    // Render board
    public void renderBoard(TetrominoType[][] board) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(OFFSET_X, 0, COLS * TILE, ROWS * TILE);


        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (board[y][x] != null) {
                    gc.setFill(board[y][x].color);
                    gc.fillRect(OFFSET_X + x * TILE, y * TILE, TILE, TILE);
                }

                gc.setStroke(Color.DARKGRAY);
                gc.strokeRect(OFFSET_X + x * TILE, y * TILE, TILE, TILE);
            }
        }
    }
    // Render current piece
    public void renderCurrentPiece(TetrominoHandler current){
        GraphicsContext gc = getGraphicsContext2D();
        if (current != null) {
            gc.setFill(current.type.color);
            for (int[] p : current.getBlocks()) {
                int x = OFFSET_X + (current.x + p[0]) * TILE;
                int y = (current.y + p[1]) * TILE;
                gc.fillRect(x, y, TILE, TILE);
            }
        }
    }
    public void renderGhostPiece(TetrominoHandler current, int ghostY) {
        if (current == null) return;

        GraphicsContext gc = getGraphicsContext2D();

        gc.setGlobalAlpha(0.3);

        gc.setFill(current.type.color);

        for (int[] p : current.getBlocks()) {
            int x = OFFSET_X + (current.x + p[0]) * TILE;
            int y = (ghostY + p[1]) * TILE;

            gc.fillRect(x, y, TILE, TILE);
        }

        gc.setGlobalAlpha(1.0);
    }
    // Render next pieces
    public void renderNext(List<TetrominoType> preview) {
        GraphicsContext gc = getGraphicsContext2D();
        // Right pane
        gc.setFill(Color.BLACK);
        gc.fillRect(OFFSET_X + COLS * TILE, 0, 100, getHeight());
        int baseX = OFFSET_X + 320;
        int baseY = 50;
        int TILE = 20;

        gc.setFill(Color.WHITE);
        gc.fillText("NEXT", baseX, baseY - 10);

        for (int i = 0; i < preview.size(); i++) {
            TetrominoType type = preview.get(i);
            gc.setFill(type.color);
            int offsetY = baseY + i * 80;

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;

            int[][] shape = type.blocks;

            for (int[] p : shape) {
                minX = Math.min(minX, p[0]);
                minY = Math.min(minY, p[1]);
            }

            for (int[] p : shape) {
                int x = baseX + (p[0] - minX) * TILE;
                int y = offsetY + (p[1] - minY) * TILE;
                gc.fillRect(x, y, TILE, TILE);
            }
        }
    }
    public void renderHold(TetrominoType hold) {
        GraphicsContext gc = getGraphicsContext2D();

        //  Hold section bg
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, OFFSET_X, getHeight());

        int baseX = 20;
        int baseY = 50;
        int TILE = 20;

        // Text
        gc.setFill(Color.WHITE);
        gc.fillText("HOLD", baseX, baseY - 10);

        if (hold == null) return;

        int[][] shape = hold.blocks;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (int[] p : shape) {
            minX = Math.min(minX, p[0]);
            minY = Math.min(minY, p[1]);
        }

        gc.setFill(hold.color);

        for (int[] p : shape) {
            int x = baseX + (p[0] - minX) * TILE;
            int y = baseY + (p[1] - minY) * TILE;

            gc.fillRect(x, y, TILE, TILE);
        }
    }
}
