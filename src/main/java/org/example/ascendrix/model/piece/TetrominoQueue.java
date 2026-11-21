package org.example.ascendrix.model.piece;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;

public class TetrominoQueue {

    public static final int PREVIEW_SIZE = 4;

    private static final List<TetrominoType> BAG_PIECES = List.of(TetrominoType.values());

    private final Queue<TetrominoType> bag = new ArrayDeque<>();
    private final Queue<TetrominoType> preview = new ArrayDeque<>();
    private final Random random;

    public TetrominoQueue() {
        this(new Random());
    }

    public TetrominoQueue(Random random) {
        this.random = Objects.requireNonNull(random, "Random cannot be null");
        reset();
    }

    /* ========================== QUEUE OPERATIONS ========================== */

    public TetrominoType next() {
        TetrominoType next = preview.poll();
        fillPreview();
        return next;
    }

    public TetrominoType peek() {
        return preview.peek();
    }

    public List<TetrominoType> getPreview() {
        return List.copyOf(preview);
    }

    public void reset() {
        bag.clear();
        preview.clear();
        refillBag();
        fillPreview();
    }

    private void refillBag() {
        List<TetrominoType> shuffled = new ArrayList<>(BAG_PIECES);
        Collections.shuffle(shuffled, random);
        bag.addAll(shuffled);
    }

    private TetrominoType getNextFromBag() {
        if (bag.isEmpty()) {
            refillBag();
        }
        return bag.poll();
    }

    private void fillPreview() {
        while (preview.size() < PREVIEW_SIZE) {
            preview.add(getNextFromBag());
        }
    }

    /* ========================== DROP TELEMETRY ========================== */

    public enum DropType {
        SOFT,
        HARD,
        NONE
    }
}