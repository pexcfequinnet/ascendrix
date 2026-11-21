package org.example.ascendrix.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.ascendrix.model.input.KeyBindingConfig;
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
    private static final Path KEYBINDINGS_PATH = CONFIG_DIR.resolve("keybindings.json");

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
        scoreManager.addScore(modeStr, playerName, sortValue, displayValue, elapsedMs);
        saveScores(scoreManager);
    }

    public Path getProfilePath()     { return PROFILE_PATH; }
    public Path getKeybindingsPath() { return KEYBINDINGS_PATH; }

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