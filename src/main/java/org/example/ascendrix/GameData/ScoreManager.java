package org.example.ascendrix.GameData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ScoreManager {
    private static final String FILE_PATH = "GameData/ScoreData/scores.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final int MAX_SCORES = 10;

    // Map chứa điểm của tất cả các Mode
    private Map<String, List<ScoreRecord>> allScores;

    public ScoreManager() {
        loadScores();
    }

    // 1. Đọc dữ liệu từ file
    private void loadScores() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<Map<String, List<ScoreRecord>>>(){}.getType();
            allScores = gson.fromJson(reader, type);
            if (allScores == null) allScores = new HashMap<>();
        } catch (FileNotFoundException e) {
            allScores = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
            allScores = new HashMap<>();
        }
    }

    // Save score into a .json file
    private void saveScores() {
        try {
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }


            try (Writer writer = new FileWriter(FILE_PATH)) {
                gson.toJson(allScores, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addScore(String modeStr, String playerName, long sortValue, String displayValue) {
        List<ScoreRecord> modeScores = allScores.computeIfAbsent(modeStr, k -> new ArrayList<>());

        modeScores.add(new ScoreRecord(playerName, sortValue, displayValue));


        if (modeStr.equals("SPRINT")) {
            modeScores.sort(Comparator.comparingLong(ScoreRecord::getSortValue));
        } else {
            // MARATHON, MASTER, OVERDRIVE: sortValue là Điểm, Rank, hoặc Level -> Càng to càng tốt (Sắp xếp Giảm dần)
            modeScores.sort((a, b) -> Long.compare(b.getSortValue(), a.getSortValue()));
        }

        if (modeScores.size() > MAX_SCORES) {
            modeScores.subList(MAX_SCORES, modeScores.size()).clear();
        }

        saveScores();
    }

    // Lấy danh sách điểm để hiển thị trên UI
    public List<ScoreRecord> getTopScores(String modeStr) {
        return allScores.getOrDefault(modeStr, new ArrayList<>());
    }
}