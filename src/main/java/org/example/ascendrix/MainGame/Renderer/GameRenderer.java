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

    // ✅ ĐÃ TÍNH TOÁN LẠI ĐỂ KHỚP TUYỆT ĐỐI 1024x768
    private final int TILE = 34; // Tăng kích thước gạch cho nét hơn (cũ: 30)
    private final int COLS = 10;
    private final int ROWS = 25;
    public final int VISIBLE_ROWS = 20;
    private final int HIDDEN_ROWS = ROWS - VISIBLE_ROWS;

    // Chiều ngang: 342 (Left) + 340 (Board) + 342 (Right) = 1024
    private final int OFFSET_X = 342;
    private final int RIGHT_PANEL = 342;

    // Chiều dọc: 44 (Top) + 680 (Board) + 44 (Bottom) = 768
    private final int TOP_PANEL = 44;
    private final int BOTTOM_PANEL = 44;

    public GameRenderer() {
        setWidth(OFFSET_X + COLS * TILE + RIGHT_PANEL);
        setHeight(TOP_PANEL + VISIBLE_ROWS * TILE + BOTTOM_PANEL);
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

        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 64)); // Phóng to font một chút

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
        gc.fillRect(OFFSET_X, TOP_PANEL, COLS * TILE, VISIBLE_ROWS * TILE + BOTTOM_PANEL);

        double centerX = OFFSET_X + (COLS * TILE) / 2.0;
        double centerY = TOP_PANEL + (VISIBLE_ROWS * TILE) / 2.0;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 50));

        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2.5);
        gc.strokeText("GAME OVER", centerX, centerY - 20);

        gc.setFill(Color.RED);
        gc.fillText("GAME OVER", centerX, centerY - 20);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("PRESS ENTER TO CONTINUE", centerX, centerY + 40); // Đổi text phù hợp luồng ENTER mới

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

        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 46));

        gc.setStroke(Color.DARKORANGE);
        gc.setLineWidth(2.5);
        gc.strokeText("GAME CLEARED", centerX, centerY - 20);

        gc.setFill(Color.GOLD);
        gc.fillText("GAME CLEARED", centerX, centerY - 20);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
        gc.setFill(Color.CYAN);
        gc.fillText("EXCELLENT!", centerX, centerY + 40);

        gc.restore();
    }

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

    public void renderNext(List<TetrominoType> preview, GameEngine game) {
        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(OFFSET_X + COLS * TILE, 0, RIGHT_PANEL, getHeight());

        int miniTile = 25; // Block to hơn cho Next queue
        int baseX = OFFSET_X + COLS * TILE + 60; // Dịch ra xa viền bảng
        int baseY = 100;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        gc.fillText("NEXT", baseX, baseY - 20);

        for (int i = 0; i < preview.size(); i++) {
            TetrominoType type = preview.get(i);
            boolean decolor = game.modeHandler.isDecolorActive();
            gc.setFill(decolor ? Color.PURPLE : type.color);
            int offsetY = baseY + i * 100;

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;

            int[][] shape = type.blocks;

            for (int[] p : shape) {
                minX = Math.min(minX, p[0]);
                minY = Math.min(minY, p[1]);
            }

            for (int[] p : shape) {
                int x = baseX + (p[0] - minX) * miniTile;
                int y = offsetY + (p[1] - minY) * miniTile;
                gc.fillRect(x, y, miniTile, miniTile);
            }
        }
    }

    public void renderHold(TetrominoType hold, GameEngine game) {
        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, OFFSET_X, getHeight());

        // ✅ Căn chỉnh lại cho Panel bên trái (rộng 342px)
        int miniTile = 25;
        int baseX = OFFSET_X - 160;  // Lùi sâu vào Panel trái để không sát viền
        int baseY = 100;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        gc.fillText("HOLD", baseX, baseY - 20);

        if (hold == null) return;

        int[][] shape = hold.blocks;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (int[] p : shape) {
            minX = Math.min(minX, p[0]);
            minY = Math.min(minY, p[1]);
        }

        boolean decolor = game.modeHandler.isDecolorActive();
        gc.setFill(decolor ? Color.PURPLE : hold.color);

        for (int[] p : shape) {
            int x = baseX + (p[0] - minX) * miniTile;
            int y = baseY + (p[1] - minY) * miniTile;

            gc.fillRect(x, y, miniTile, miniTile);
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

        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 56));

        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillText(mainText, centerX + 5, centerY + 5);

        gc.setStroke(Color.DARKORANGE);
        gc.setLineWidth(4.0);
        gc.strokeText(mainText, centerX, centerY);

        gc.setFill(Color.GOLD);
        gc.fillText(mainText, centerX, centerY);

        String subText = "- PERFECT -";
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 26));

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        gc.strokeText(subText, centerX, centerY + 55);

        gc.setFill(Color.WHITE);
        gc.fillText(subText, centerX, centerY + 55);

        gc.restore();
    }
}