package org.example.ascendrix.MainGame.Renderer;

import org.example.ascendrix.Tetromino.TetrominoType;

public class BoardRenderContext {

    public interface CellAlphaProvider {
        double getAlpha(int x, int y);
    }

    public final TetrominoType[][] board;
    public final CellAlphaProvider alphaProvider;
    public final boolean invisible;

    public BoardRenderContext(TetrominoType[][] board, CellAlphaProvider alphaProvider, boolean invisible) {
        this.board = board;
        this.alphaProvider = alphaProvider;
        this.invisible = invisible;
    }
}