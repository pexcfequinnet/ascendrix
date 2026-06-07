package org.example.ascendrix.GameData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ScoreManager {
    private static final String FILE_PATH = "GameData/ScoreData/scores.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // PrettyPrint để file JSON đẹp, dễ đọc

    // ✅ THÊM: Hằng số quy định số lượng kỷ lục tối đa trên bảng xếp hạng
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
            // File chưa tồn tại (chơi lần đầu), tạo Map trống
            allScores = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
            allScores = new HashMap<>();
        }
    }

    // 2. Lưu dữ liệu xuống file
    private void saveScores() {
        try {
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();

            // Nếu thư mục ScoreData chưa tồn tại, tự động tạo nó trước!
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // ✅ SỬA: Đã xóa đoạn parentDir.mkdirs() bị lặp thừa thứ 2

            try (Writer writer = new FileWriter(FILE_PATH)) {
                gson.toJson(allScores, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 3. Thêm điểm mới và sắp xếp lại Top
    public void addScore(String modeStr, String playerName, long sortValue, String displayValue) {
        // 1. Lấy danh sách điểm của Mode hiện tại (Nếu chưa có thì tạo list mới)
        List<ScoreRecord> modeScores = allScores.computeIfAbsent(modeStr, k -> new ArrayList<>());

        // 2. Thêm kỷ lục mới vào danh sách
        modeScores.add(new ScoreRecord(playerName, sortValue, displayValue));

        // 3. Phân loại thuật toán sắp xếp dựa theo Game Mode
        if (modeStr.equals("SPRINT")) {
            // SPRINT: sortValue là thời gian -> Càng nhỏ càng tốt (Sắp xếp Tăng dần)
            modeScores.sort(Comparator.comparingLong(ScoreRecord::getSortValue));
        } else {
            // MARATHON, MASTER, OVERDRIVE: sortValue là Điểm, Rank, hoặc Level -> Càng to càng tốt (Sắp xếp Giảm dần)
            modeScores.sort((a, b) -> Long.compare(b.getSortValue(), a.getSortValue()));
        }

        // 4. ✅ SỬA: Cắt tỉa danh sách dựa trên hằng số MAX_SCORES (Top 10)
        if (modeScores.size() > MAX_SCORES) {
            modeScores.subList(MAX_SCORES, modeScores.size()).clear();
        }

        // 5. Ghi thẳng xuống file JSON để lưu trữ vĩnh viễn
        saveScores();
    }

    // 4. Lấy danh sách điểm để hiển thị trên UI
    public List<ScoreRecord> getTopScores(String modeStr) {
        return allScores.getOrDefault(modeStr, new ArrayList<>());
    }
}