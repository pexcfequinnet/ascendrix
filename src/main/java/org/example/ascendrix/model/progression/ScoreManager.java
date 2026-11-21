package org.example.ascendrix.model.progression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScoreManager {
    private static final int MAX_SCORES = 10;

    private final Map<String, List<ScoreRecord>> allScores = new HashMap<>();

    public ScoreManager() {
    }

    public void replaceAll(Map<String, List<ScoreRecord>> loadedScores) {
        allScores.clear();
        if (loadedScores != null) {
            loadedScores.forEach((mode, records) -> {
                if (records != null) {
                    allScores.put(mode, new ArrayList<>(records));
                }
            });
        }
    }

    public void addScore(String modeStr, String playerName, long sortValue, String displayValue) {
        addScore(modeStr, playerName, sortValue, displayValue, 0L);
    }

    public void addScore(String modeStr, String playerName, long sortValue, String displayValue, long elapsedMs) {
        List<ScoreRecord> modeScores = allScores.computeIfAbsent(modeStr, k -> new ArrayList<>());
        modeScores.add(new ScoreRecord(playerName, sortValue, displayValue, elapsedMs));

        if ("SPRINT".equalsIgnoreCase(modeStr)) {
            // SPRINT: sortValue is elapsed time in ms -> lower is better (Ascending)
            modeScores.sort(Comparator.comparingLong(ScoreRecord::getSortValue));
        } else {
            // MARATHON, ULTRA, MASTER, OVERDRIVE: sortValue is Score, Rank, or Level -> higher is better (Descending)
            modeScores.sort(Comparator.comparingLong(ScoreRecord::getSortValue).reversed());
        }

        if (modeScores.size() > MAX_SCORES) {
            modeScores.subList(MAX_SCORES, modeScores.size()).clear();
        }
    }

    public List<ScoreRecord> getTopScores(String modeStr) {
        List<ScoreRecord> scores = allScores.get(modeStr);
        if (scores == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(scores);
    }

    public Optional<ScoreRecord> getBestScore(String modeStr) {
        List<ScoreRecord> scores = allScores.get(modeStr);
        if (scores == null || scores.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(scores.get(0));
    }

    public Map<String, List<ScoreRecord>> getAllScoresSnapshot() {
        Map<String, List<ScoreRecord>> snapshot = new HashMap<>();
        allScores.forEach((mode, records) -> snapshot.put(mode, new ArrayList<>(records)));
        return Collections.unmodifiableMap(snapshot);
    }
}