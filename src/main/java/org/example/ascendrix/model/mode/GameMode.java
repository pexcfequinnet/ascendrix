package org.example.ascendrix.model.mode;

public enum GameMode {
    SPRINT("40 LINES", "#00C8C8", true),
    ULTRA("ULTRA", "#FFA726", false),
    MARATHON("MARATHON", "#66BB6A", true),
    MASTER("MASTER", "#42A5F5", false),
    MASTER_11("MASTER 1.1", "#AB47BC", false),
    OVERDRIVE("OVERDRIVE", "#EF5350", false);

    private final String displayName;
    private final String accentColor;
    private final boolean allowsCustomHandling;

    GameMode(String displayName, String accentColor, boolean allowsCustomHandling) {
        this.displayName = displayName;
        this.accentColor = accentColor;
        this.allowsCustomHandling = allowsCustomHandling;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public boolean allowsCustomHandling() {
        return allowsCustomHandling;
    }
}