package org.example.ascendrix.GameData;

public class ScoreRecord {
    private String playerName;
    private long sortValue;      // Số dùng để sắp xếp
    private String displayValue; // Chuỗi dùng để in ra màn hình

    // Bổ sung tham số thứ 3 (displayValue) vào Constructor
    public ScoreRecord(String playerName, long sortValue, String displayValue) {
        this.playerName = playerName;
        this.sortValue = sortValue;
        this.displayValue = displayValue;
    }

    public String getPlayerName() { return playerName; }
    public long getSortValue() { return sortValue; }
    public String getDisplayValue() { return displayValue; }

}
