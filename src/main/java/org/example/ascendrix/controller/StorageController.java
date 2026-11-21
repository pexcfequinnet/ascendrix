package org.example.ascendrix.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.ascendrix.model.input.KeyBindingConfig;
import org.example.ascendrix.model.mechanics.HandlingConfig;
import org.example.ascendrix.model.progression.ScoreManager;
import org.example.ascendrix.model.progression.ScoreRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class StorageController {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path CONFIG_DIR = Paths.get("config");

    private static final Path SCORES_PATH = DATA_DIR.resolve("scores.json");
    private static final Path PROFILE_PATH = DATA_DIR.resolve("profile.json");
    private static final Path HANDLING_PATH = DATA_DIR.resolve("handling.json");
    private static final Path KEYBINDINGS_PATH = CONFIG_DIR.resolve("keybindings.json");
    private static final Type HANDLING_MAP_TYPE = TypeToken.getParameterized(
            Map.class,
            String.class,
            Object.class
    ).getType();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SCORE_MAP_TYPE = TypeToken.getParameterized(
            Map.class,
            String.class,
            TypeToken.getParameterized(List.class, ScoreRecord.class).getType()
    ).getType();

    public void loadScores(ScoreManager scoreManager) {
        if (!Files.exists(SCORES_PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(SCORES_PATH, StandardCharsets.UTF_8)) {
            Map<String, List<ScoreRecord>> loaded = GSON.fromJson(reader, SCORE_MAP_TYPE);
            scoreManager.replaceAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveScores(ScoreManager scoreManager) {
        try {
            if (SCORES_PATH.getParent() != null) {
                Files.createDirectories(SCORES_PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(SCORES_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(scoreManager.getAllScoresSnapshot(), writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordAndSaveScore(ScoreManager scoreManager, String modeStr, String playerName, long sortValue, String displayValue) {
        recordAndSaveScore(scoreManager, modeStr, playerName, sortValue, displayValue, 0L);
    }

    public void recordAndSaveScore(ScoreManager scoreManager, String modeStr, String playerName, long sortValue, String displayValue, long elapsedMs) {
        if ("SPRINT".equalsIgnoreCase(modeStr) && (sortValue <= 0L || elapsedMs <= 0L)) {
            return;
        }
        long resolvedElapsedMs = (elapsedMs <= 0L && "SPRINT".equalsIgnoreCase(modeStr) && sortValue > 0L)
                ? sortValue
                : Math.max(0L, elapsedMs);
        scoreManager.addScore(modeStr, playerName, sortValue, displayValue, resolvedElapsedMs);
        saveScores(scoreManager);
    }

    public Path getProfilePath()     { return PROFILE_PATH; }
    public Path getKeybindingsPath() { return KEYBINDINGS_PATH; }

    // Handling config
    public HandlingConfig loadHandling() {
        if (!Files.exists(HANDLING_PATH)) {
            return HandlingConfig.defaultConfig();
        }
        try (Reader reader = Files.newBufferedReader(HANDLING_PATH, StandardCharsets.UTF_8)) {
            Map<String, Object> raw = GSON.fromJson(reader, HANDLING_MAP_TYPE);
            if (raw == null || raw.isEmpty()) {
                return HandlingConfig.defaultConfig();
            }
            long dasNs = getLong(raw, "dasNs", HandlingConfig.DEFAULT_DAS_NS);
            long arrNs = getLong(raw, "arrNs", HandlingConfig.DEFAULT_ARR_NS);
            long sdfNs = getLong(raw, "sdfNs", HandlingConfig.DEFAULT_SDF_NS);
            boolean instantArr = Boolean.TRUE.equals(raw.get("instantArr"));
            boolean instantSdf = Boolean.TRUE.equals(raw.get("instantSdf"));
            return new HandlingConfig(dasNs, arrNs, sdfNs, instantArr, instantSdf);
        } catch (Exception e) {
            System.err.println("Error reading handling config: " + e.getMessage());
            return HandlingConfig.defaultConfig();
        }
    }

    public void saveHandling(HandlingConfig config) {
        if (config == null) return;
        try {
            if (HANDLING_PATH.getParent() != null) {
                Files.createDirectories(HANDLING_PATH.getParent());
            }
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("dasNs", config.getDasNs());
            data.put("arrNs", config.getArrNs());
            data.put("sdfNs", config.getSdfNs());
            data.put("instantArr", config.isInstantArr());
            data.put("instantSdf", config.isInstantSdf());

            try (Writer writer = Files.newBufferedWriter(HANDLING_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving handling config: " + e.getMessage());
        }
    }


    private long getLong(Map<String, Object> map, String key, long fallback) {
        Object val = map.get(key);
        return (val instanceof Number num) ? num.longValue() : fallback;
    }

    // Keybind config
    private static final Path KEYBINDS_PATH = DATA_DIR.resolve("keybinds.json");
    private static final Type KEYBIND_MAP_TYPE = TypeToken.getParameterized(
            Map.class,
            String.class,
            String.class
    ).getType();

    public boolean loadKeybinds(KeyBindingConfig config) {
        if (!Files.exists(KEYBINDS_PATH)) {
            return false;
        }
        try (Reader reader = Files.newBufferedReader(KEYBINDS_PATH, StandardCharsets.UTF_8)) {
            Map<String, String> loaded = GSON.fromJson(reader, KEYBIND_MAP_TYPE);
            if (loaded == null || loaded.isEmpty()) {
                return false;
            }
            config.replaceAll(loaded);
            return true;
        } catch (Exception e) {
            System.err.println("Error reading keybinds config: " + e.getMessage());
            return false;
        }
    }

    public void saveKeybinds(KeyBindingConfig config) {
        try {
            if (KEYBINDS_PATH.getParent() != null) {
                Files.createDirectories(KEYBINDS_PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(KEYBINDS_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(config.toSerializableMap(), writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving keybinds config: " + e.getMessage());
        }
    }
}