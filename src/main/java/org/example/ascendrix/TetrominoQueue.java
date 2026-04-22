package org.example.ascendrix;

import java.util.*;

public class TetrominoQueue {

    private final Queue<TetrominoType> bag = new LinkedList<>();
    private final Queue<TetrominoType> preview = new LinkedList<>();

    public TetrominoQueue() {
        refillBag();
        fillPreview();
    }

    private void refillBag() {
        List<TetrominoType> list = new ArrayList<>(Arrays.asList(TetrominoType.values()));
        Collections.shuffle(list);
        bag.addAll(list);
    }

    private TetrominoType getNextFromBag() {
        if (bag.isEmpty()) {
            refillBag();
        }
        return bag.poll();
    }

    private void fillPreview() {
        int PREVIEW_SIZE = 4;
        while (preview.size() < PREVIEW_SIZE) {
            preview.add(getNextFromBag());
        }
    }

    // ===== Public API =====

    public TetrominoType next() {
        TetrominoType next = preview.poll();
        fillPreview();
        return next;
    }

    public List<TetrominoType> getPreview() {
        return new ArrayList<>(preview);
    }
}