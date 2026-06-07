package org.example.ascendrix.MainGame.Renderer;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.example.ascendrix.MainGame.Engine.GameEngine;
import org.example.ascendrix.MainGame.Engine.GamePhase;
import org.example.ascendrix.MainGame.Engine.GameTimer;
import org.example.ascendrix.GameMode.GameModeHandler;
import org.example.ascendrix.Tetromino.TetrominoHandler;
import org.example.ascendrix.Tetromino.TetrominoType;

import java.util.*;


public class GameRenderer extends Canvas {

    private final int TILE = 30;
    private final int COLS = 10;
    private final int ROWS = 25;
    public final int VISIBLE_ROWS = 20;
    private final int HIDDEN_ROWS = ROWS - VISIBLE_ROWS;
    private final int OFFSET_X = 250; // Move entire board to the right to make space for HUD

    private final int TOP_PANEL = 50;
    private final int RIGHT_PANEL = 250;
    private final int BOTTOM_PANEL = 50;
    public GameRenderer() {
        setWidth(OFFSET_X + COLS * TILE + RIGHT_PANEL);
        setHeight(TOP_PANEL + VISIBLE_ROWS * TILE + BOTTOM_PANEL); // 700px total
    }


    public void renderCountdown(GamePhase phase, int countdown) {
        if (phase != GamePhase.COUNTDOWN) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.save();

        String text;
        Color textColor;
        Color strokeColor;

        if (countdown > 1) {
            text = String.valueOf(countdown);
            textColor = Color.WHITE;
            strokeColor = Color.GRAY;
        } else if (countdown == 1) {
            text = "READY";
            textColor = Color.GOLD;
            strokeColor = Color.DARKORANGE;
        } else {
            text = "GO!";
            textColor = Color.LIMEGREEN;
            strokeColor = Color.DARKGREEN;
        }

        double centerX = OFFSET_X + (COLS * TILE) / 2.0;
        double centerY = TOP_PANEL + (VISIBLE_ROWS * TILE) / 2.0;
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 54));

        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillText(text, centerX + 4, centerY + 4);

        gc.setStroke(strokeColor);
        gc.setLineWidth(3.0);
        gc.strokeText(text, centerX, centerY);

        gc.setFill(textColor);
        gc.fillText(text, centerX, centerY);

        gc.restore();
    }

    public void renderGameOver() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();

        gc.setFill(Color.color(0, 0, 0, 0.75));
        // Sửa lại tọa độ Y và Chiều cao của lớp nền che mờ
        gc.fillRect(OFFSET_X, TOP_PANEL, COLS * TILE, VISIBLE_ROWS * TILE + BOTTOM_PANEL);

        double centerX = OFFSET_X + (COLS * TILE) / 2.0;
        // Cộng thêm TOP_PANEL vào trục Y
        double centerY = TOP_PANEL + (VISIBLE_ROWS * TILE) / 2.0;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 44));

        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2.5);
        gc.strokeText("GAME OVER", centerX, centerY - 20);

        gc.setFill(Color.RED);
        gc.fillText("GAME OVER", centerX, centerY - 20);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("PRESS RESTART", centerX, centerY + 30);

        gc.restore();
    }

    public void renderGameComplete() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();

        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillRect(OFFSET_X, TOP_PANEL, COLS * TILE, VISIBLE_ROWS * TILE + BOTTOM_PANEL);

        double centerX = OFFSET_X + (COLS * TILE) / 2.0;
        double centerY = TOP_PANEL + (VISIBLE_ROWS * TILE) / 2.0;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 40));

        gc.setStroke(Color.DARKORANGE);
        gc.setLineWidth(2.5);
        gc.strokeText("GAME CLEARED", centerX, centerY - 20);

        gc.setFill(Color.GOLD);
        gc.fillText("GAME CLEARED", centerX, centerY - 20);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        gc.setFill(Color.CYAN);
        gc.fillText("EXCELLENT!", centerX, centerY + 30);

        gc.restore();
    }

    // Render board
    public void renderBoard(TetrominoType[][] board, BoardRenderContext ctx, GameEngine game) {
        int hiddenRows = board.length - VISIBLE_ROWS;

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(OFFSET_X, 0, COLS * TILE, getHeight());

        for (int y = hiddenRows; y < board.length; y++) {
            for (int x = 0; x < COLS; x++) {
                int screenY = (y - hiddenRows) * TILE + TOP_PANEL;

                if (game.isGarbageCell(y, x)) {
                    gc.setFill(Color.GRAY);
                    gc.fillRect(OFFSET_X + x * TILE, screenY, TILE, TILE);
                } else if (board[y][x] != null) {
                    double alpha = (ctx != null && ctx.alphaProvider != null)
                            ? ctx.alphaProvider.getAlpha(x, y)
                            : 1.0;
                    if (alpha > 0) {
                        gc.setGlobalAlpha(alpha);
                        gc.setFill(board[y][x].color);
                        gc.fillRect(OFFSET_X + x * TILE, screenY, TILE, TILE);
                        gc.setGlobalAlpha(1.0);
                    }
                }
                gc.setStroke(Color.DARKGRAY);
                gc.strokeRect(OFFSET_X + x * TILE, screenY, TILE, TILE);
            }
        }
    }
    // Render current piece
    public void renderCurrentPiece(TetrominoHandler current, GameEngine game) {
        GraphicsContext gc = getGraphicsContext2D();
        if (current != null) {
            boolean decolor = game.modeHandler.isDecolorActive();
            gc.setFill(decolor ? Color.PURPLE : current.type.color);
            for (int[] p : current.getBlocks()) {
                int screenY = current.y + p[1] - HIDDEN_ROWS;
                if (screenY < 0) continue;
                int x = OFFSET_X + (current.x + p[0]) * TILE;
                int y = (screenY) * TILE + TOP_PANEL;
                gc.fillRect(x, y, TILE, TILE);
            }
        }
    }

    // Render ghost piece
    public void renderGhostPiece(TetrominoHandler current, int ghostY, GameEngine game) {
        if (current == null) return;
        GraphicsContext gc = getGraphicsContext2D();
        boolean decolor = game.modeHandler.isDecolorActive();
        gc.setGlobalAlpha(0.7);
        gc.setFill(decolor ? Color.PURPLE : current.type.color);
        for (int[] p : current.getBlocks()) {
            int screenY = ghostY + p[1] - HIDDEN_ROWS;
            if (screenY < 0) continue;
            int x = OFFSET_X + (current.x + p[0]) * TILE;
            int y = (screenY) * TILE + TOP_PANEL;
            gc.fillRect(x, y, TILE, TILE);
        }
        gc.setGlobalAlpha(1.0);
    }
    // Render next pieces
    public void renderNext(List<TetrominoType> preview, GameEngine game) {
        GraphicsContext gc = getGraphicsContext2D();

        // Fill the full right panel instead of just 100px
        gc.setFill(Color.BLACK);
        gc.fillRect(OFFSET_X + COLS * TILE, 0, RIGHT_PANEL, getHeight());

        int baseX = OFFSET_X + COLS * TILE + 20; // 20px padding from board edge
        int baseY = 50;
        int TILE = 20;

        gc.setFill(Color.WHITE);
        gc.fillText("NEXT", baseX, baseY - 10);

        for (int i = 0; i < preview.size(); i++) {
            TetrominoType type = preview.get(i);
            boolean decolor = game.modeHandler.isDecolorActive();
            gc.setFill(decolor ? Color.PURPLE : type.color);
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

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, OFFSET_X, getHeight());

        int TILE = 20;
        int baseX = OFFSET_X - 80;  // sits just to the left of the board
        int baseY = 50;

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

    public void renderHUD(GameModeHandler modeHandler, GameTimer timer, long now){
        GraphicsContext gc = getGraphicsContext2D();
        if (modeHandler != null) {
            modeHandler.renderHUD(gc, timer, now);
        }
    }
    public void renderPerfectClear() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();

        double centerX = OFFSET_X + (COLS * TILE) / 2.0;
        double centerY = TOP_PANEL + (VISIBLE_ROWS * TILE) / 2.0;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        String mainText = "ALL CLEAR!";

        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 52));

        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillText(mainText, centerX + 5, centerY + 5);

        gc.setStroke(Color.DARKORANGE);
        gc.setLineWidth(4.0);
        gc.strokeText(mainText, centerX, centerY);

        gc.setFill(Color.GOLD);
        gc.fillText(mainText, centerX, centerY);

        String subText = "- PERFECT -";
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 22));

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        gc.strokeText(subText, centerX, centerY + 45);

        gc.setFill(Color.WHITE);
        gc.fillText(subText, centerX, centerY + 45);

        gc.restore();
    }
}
