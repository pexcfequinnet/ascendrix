package org.example.ascendrix.view.render;

import org.example.ascendrix.model.piece.TetrominoType;

public class BoardRenderContext {

    @FunctionalInterface
    public interface CellAlphaProvider {
        double getAlpha(int x, int y);
    }

    public final TetrominoType[][] board;
    public final CellAlphaProvider alphaProvider;
    public final boolean invisible;

    public BoardRenderContext(TetrominoType[][] board, CellAlphaProvider alphaProvider, boolean invisible) {
        this.board = board;
        this.alphaProvider = (alphaProvider != null) ? alphaProvider : (x, y) -> 1.0;
        this.invisible = invisible;
    }

    public double getAlpha(int x, int y) {
        return alphaProvider.getAlpha(x, y);
    }
}