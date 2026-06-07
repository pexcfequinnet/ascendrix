package org.example.ascendrix.Tetromino;

import java.util.*;
import java.util.stream.Collectors;

public class TetrominoQueue {

    private final Queue<TetrominoType> bag = new LinkedList<>();
    private final Queue<TetrominoType> preview = new LinkedList<>();

    public TetrominoQueue() {
        refillBag();
        fillPreview();
    }

    private void refillBag() {
        List<TetrominoType> list = Arrays.stream(TetrominoType.values())
                .filter(t -> t != TetrominoType.GARBAGE && t != TetrominoType.BONE)
                .collect(Collectors.toList());
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

    // API: Next queue

    public TetrominoType next() {
        TetrominoType next = preview.poll();
        fillPreview();
        return next;
    }

    public List<TetrominoType> getPreview() {
        return new ArrayList<>(preview);
    }

    public enum DropType {
        SOFT,
        HARD,
        NONE,
    }
}