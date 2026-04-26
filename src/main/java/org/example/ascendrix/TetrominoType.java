package org.example.ascendrix;
import javafx.scene.paint.Color;

public enum TetrominoType {

    I(new int[][]{
            // 0
                    {0, 1}, {1, 1}, {2, 1}, {3, 1}
    }, Color.CYAN),

    O(new int[][]{
        {1, 0}, {2, 0},
        {1, 1}, {2, 1},
    }, Color.YELLOW),

    T(new int[][]{
            {0,1},{1,1},{2,1},{1,0}
    }, Color.PURPLE),

    S(new int[][]{
        {1, 0}, {2, 0}, {0, 1}, {1, 1}
    }, Color.LIGHTGREEN),

    Z(new int[][]{
                    {0,0},{1,0},{1,1},{2,1}
    }, Color.RED),

    J(new int[][]{
            {0, 0}, {0, 1}, {1, 1}, {2, 1}
    }, Color.BLUE),

    L(new int[][]{
        {2,0},{0,1},{1,1},{2,1}
    }, Color.ORANGE);

    public final int[][] blocks;
    public final Color color;

    TetrominoType(int[][] blocks, Color color) {
        this.blocks = blocks;
        this.color = color;
    }
}
