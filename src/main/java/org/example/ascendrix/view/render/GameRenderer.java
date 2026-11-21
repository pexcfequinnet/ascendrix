package org.example.ascendrix.view.render;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.ascendrix.model.board.Board;
import org.example.ascendrix.model.board.FadeMap;
import org.example.ascendrix.model.mode.ModeRuleset;
import org.example.ascendrix.model.piece.ActivePiece;
import org.example.ascendrix.model.piece.TetrominoType;
import org.example.ascendrix.model.state.GamePhase;
import org.example.ascendrix.model.state.GameSession;
import org.example.ascendrix.model.state.GameTimer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GameRenderer extends Canvas {

    // Resolution: 1024x768 -> X: 342 + (10 * 34) + 342 = 1024 | Y: 44 + (20 * 34) + 44 = 768
    private static final int TILE = 34;
    private static final int VISIBLE_ROWS = 20;
    private static final int HIDDEN_ROWS = Board.ROWS - VISIBLE_ROWS;

    private static final int OFFSET_X = 342;
    private static final int RIGHT_PANEL = 342;
    private static final int TOP_PANEL = 44;
    private static final int BOTTOM_PANEL = 44;

    private static final Color BONE_COLOR = Color.PURPLE;
    private static final Color GARBAGE_COLOR = Color.GRAY;
    private static final Map<TetrominoType, Color> PIECE_COLORS = new EnumMap<>(TetrominoType.class);

    static {
        PIECE_COLORS.put(TetrominoType.I, Color.CYAN);
        PIECE_COLORS.put(TetrominoType.J, Color.DODGERBLUE);
        PIECE_COLORS.put(TetrominoType.L, Color.ORANGE);
        PIECE_COLORS.put(TetrominoType.O, Color.GOLD);
        PIECE_COLORS.put(TetrominoType.S, Color.LIMEGREEN);
        PIECE_COLORS.put(TetrominoType.T, Color.MEDIUMORCHID);
        PIECE_COLORS.put(TetrominoType.Z, Color.CRIMSON);
    }

    private final HUDRenderDispatcher hudRenderer = new HUDRenderDispatcher();

    public GameRenderer() {
        setWidth(OFFSET_X + Board.COLS * TILE + RIGHT_PANEL);
        setHeight(TOP_PANEL + VISIBLE_ROWS * TILE + BOTTOM_PANEL);
    }

    /* ========================== MAIN FRAME RENDER ========================== */

    public void render(GameSession session, long now) {
        Board board = session.getBoard();
        ModeRuleset ruleset = session.getRuleset();
        boolean decolor = ruleset.isDecolorActive();

        BoardRenderContext ctx = buildBoardContext(board, ruleset);
        renderBoard(board, ctx);

        if (session.getPhase() == GamePhase.GAME_OVER) {
            renderGameOver();
        } else if (session.getPhase() == GamePhase.CLEARED) {
            renderGameComplete();
        }

        ActivePiece current = session.getCurrentPiece();
        if (!session.isSpawning() && !session.isWaitingForClearAnim() && current != null) {
            renderGhostPiece(current, session.getGhostY(), decolor);
            renderCurrentPiece(current, decolor);
        }

        if (session.isPerfectClearVisible(now)) {
            renderPerfectClear();
        }

        renderNext(session.getQueue().getPreview(), decolor);
        renderHold(session.getHoldPiece(), decolor);
        renderHUD(ruleset, session.getTimer(), now);
        renderCountdown(session.getPhase(), session.getCountdown());
    }

    private BoardRenderContext buildBoardContext(Board board, ModeRuleset ruleset) {
        FadeMap fadeMap = ruleset.getFadeMap();
        BoardRenderContext.CellAlphaProvider alphaProvider =
                (fadeMap != null) ? fadeMap::getAlpha : null;
        return new BoardRenderContext(board.getRawGrid(), alphaProvider, ruleset.isInvisible());
    }

    private Color getPieceColor(TetrominoType type) {
        return PIECE_COLORS.getOrDefault(type, Color.WHITE);
    }

    /* ========================== BOARD & PIECES ========================== */

    public void renderBoard(Board board, BoardRenderContext ctx) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(OFFSET_X, 0, Board.COLS * TILE, getHeight());

        boolean hidden = (ctx != null && ctx.invisible);

        for (int y = HIDDEN_ROWS; y < Board.ROWS; y++) {
            int screenY = (y - HIDDEN_ROWS) * TILE + TOP_PANEL;

            for (int x = 0; x < Board.COLS; x++) {
                if (board.isGarbageCell(y, x)) {
                    gc.setFill(GARBAGE_COLOR);
                    gc.fillRect(OFFSET_X + x * TILE, screenY, TILE, TILE);
                } else if (!hidden && board.getCell(y, x) != null) {
                    double alpha = (ctx != null) ? ctx.getAlpha(x, y) : 1.0;
                    if (alpha > 0) {
                        gc.setGlobalAlpha(alpha);
                        Color fill = board.isBoneCell(y, x)
                                ? BONE_COLOR
                                : getPieceColor(board.getCell(y, x));
                        gc.setFill(fill);
                        gc.fillRect(OFFSET_X + x * TILE, screenY, TILE, TILE);
                        gc.setGlobalAlpha(1.0);
                    }
                }
                gc.setStroke(Color.DARKGRAY);
                gc.strokeRect(OFFSET_X + x * TILE, screenY, TILE, TILE);
            }
        }
    }

    public void renderCurrentPiece(ActivePiece current, boolean decolor) {
        if (current == null) return;
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(decolor ? Color.PURPLE : getPieceColor(current.getType()));

        for (int[] p : current.getBlocks()) {
            int screenY = current.getY() + p[1] - HIDDEN_ROWS;
            if (screenY < 0) continue;
            int x = OFFSET_X + (current.getX() + p[0]) * TILE;
            int y = screenY * TILE + TOP_PANEL;
            gc.fillRect(x, y, TILE, TILE);
        }
    }

    public void renderGhostPiece(ActivePiece current, int ghostY, boolean decolor) {
        if (current == null) return;
        GraphicsContext gc = getGraphicsContext2D();
        gc.setGlobalAlpha(0.35);
        gc.setFill(decolor ? Color.PURPLE : getPieceColor(current.getType()));

        for (int[] p : current.getBlocks()) {
            int screenY = ghostY + p[1] - HIDDEN_ROWS;
            if (screenY < 0) continue;
            int x = OFFSET_X + (current.getX() + p[0]) * TILE;
            int y = screenY * TILE + TOP_PANEL;
            gc.fillRect(x, y, TILE, TILE);
        }
        gc.setGlobalAlpha(1.0);
    }

    /* ========================== SIDE PANELS & HUD ========================== */

    public void renderNext(List<TetrominoType> preview, boolean decolor) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(OFFSET_X + Board.COLS * TILE, 0, RIGHT_PANEL, getHeight());

        int miniTile = 25;
        int baseX = OFFSET_X + Board.COLS * TILE + 60;
        int baseY = 100;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        gc.fillText("NEXT", baseX, baseY - 20);

        for (int i = 0; i < preview.size(); i++) {
            TetrominoType type = preview.get(i);
            gc.setFill(decolor ? Color.PURPLE : getPieceColor(type));
            int offsetY = baseY + i * 100;

            int[][] shape = type.blocks;
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
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

    public void renderHold(TetrominoType hold, boolean decolor) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, OFFSET_X, getHeight());

        int miniTile = 25;
        int baseX = OFFSET_X - 160;
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

        gc.setFill(decolor ? Color.PURPLE : getPieceColor(hold));
        for (int[] p : shape) {
            int x = baseX + (p[0] - minX) * miniTile;
            int y = baseY + (p[1] - minY) * miniTile;
            gc.fillRect(x, y, miniTile, miniTile);
        }
    }

    public void renderHUD(ModeRuleset ruleset, GameTimer timer, long now) {
        GraphicsContext gc = getGraphicsContext2D();
        if (ruleset != null) {
            hudRenderer.render(gc, ruleset, timer, now);
        }
    }

    /* ========================== OVERLAYS ========================== */

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

        double centerX = OFFSET_X + (Board.COLS * TILE) / 2.0;
        double centerY = TOP_PANEL + (VISIBLE_ROWS * TILE) / 2.0;
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 64));

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
        gc.fillRect(OFFSET_X, TOP_PANEL, Board.COLS * TILE, VISIBLE_ROWS * TILE + BOTTOM_PANEL);

        double centerX = OFFSET_X + (Board.COLS * TILE) / 2.0;
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
        gc.fillText("PRESS ENTER TO CONTINUE", centerX, centerY + 40);

        gc.restore();
    }

    public void renderGameComplete() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();

        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillRect(OFFSET_X, TOP_PANEL, Board.COLS * TILE, VISIBLE_ROWS * TILE + BOTTOM_PANEL);

        double centerX = OFFSET_X + (Board.COLS * TILE) / 2.0;
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

    public void renderPerfectClear() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();

        double centerX = OFFSET_X + (Board.COLS * TILE) / 2.0;
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