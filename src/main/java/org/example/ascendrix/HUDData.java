package org.example.ascendrix;

public class HUDData {
    public int level;
    public int nextLevel;
    public String grade;
    public long time; // milliseconds (có thể bỏ qua tạm)

    public HUDData(int level, int nextLevel, String grade, long time) {
        this.level = level;
        this.nextLevel = nextLevel;
        this.grade = grade;
        this.time = time;
    }
}