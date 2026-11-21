package org.example.ascendrix.model.progression;

public class ScoreRecord {
    private final String playerName;
    private final long sortValue;      // Asc ms for SPRINT; Desc score/grade/level for other modes
    private final String displayValue; // Primary metric (or empty for SPRINT)
    private final long elapsedMs;      // Run duration in ms across all modes

    @SuppressWarnings("unused")
    private ScoreRecord() {
        this.playerName = "UNNAMED";
        this.sortValue = 0L;
        this.displayValue = "";
        this.elapsedMs = 0L;
    }

    public ScoreRecord(String playerName, long sortValue, String displayValue) {
        this(playerName, sortValue, displayValue, 0L);
    }

    public ScoreRecord(String playerName, long sortValue, String displayValue, long elapsedMs) {
        this.playerName = (playerName == null || playerName.isBlank()) ? "PLAYER" : playerName.trim();
        this.sortValue = sortValue;
        this.displayValue = (displayValue != null) ? displayValue : String.valueOf(sortValue);
        this.elapsedMs = Math.max(0L, elapsedMs);
    }

    public String getPlayerName()   { return playerName; }
    public long getSortValue()      { return sortValue; }
    public String getDisplayValue() { return displayValue; }
    public long getElapsedMs()      { return elapsedMs; }
}