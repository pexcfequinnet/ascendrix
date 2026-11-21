package org.example.ascendrix.model.input;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class KeyBindingConfig {

    public enum InputAction {
        LEFT,
        RIGHT,
        SOFT_DROP,
        HARD_DROP,
        ROTATE_CCW,
        ROTATE_CW,
        HOLD
    }

    private final Map<InputAction, String> actionToKey = new EnumMap<>(InputAction.class);
    private final Map<String, InputAction> keyToAction = new HashMap<>();

    public KeyBindingConfig() {
        resetToDefaults();
    }

    public void resetToDefaults() {
        actionToKey.clear();
        keyToAction.clear();

        bind(InputAction.LEFT, "A");
        bind(InputAction.RIGHT, "D");
        bind(InputAction.SOFT_DROP, "S");
        bind(InputAction.HARD_DROP, "W");
        bind(InputAction.ROTATE_CCW, "J");
        bind(InputAction.ROTATE_CW, "L");
        bind(InputAction.HOLD, "SHIFT");
    }

    public void bind(InputAction action, String keyName) {
        if (action == null || keyName == null || keyName.isBlank()) return;
        String normalizedKey = keyName.trim().toUpperCase();

        String oldKey = actionToKey.get(action);
        if (oldKey != null) {
            keyToAction.remove(oldKey);
        }

        InputAction conflict = keyToAction.get(normalizedKey);
        if (conflict != null && conflict != action) {
            actionToKey.remove(conflict);
        }

        actionToKey.put(action, normalizedKey);
        keyToAction.put(normalizedKey, action);
    }

    public void replaceAll(Map<String, String> rawBindings) {
        if (rawBindings == null || rawBindings.isEmpty()) return;

        actionToKey.clear();
        keyToAction.clear();

        for (Map.Entry<String, String> entry : rawBindings.entrySet()) {
            try {
                InputAction action = InputAction.valueOf(entry.getKey());
                bind(action, entry.getValue());
            } catch (IllegalArgumentException ignored) {
                // Skip unknown action keys from older config files
            }
        }
    }

    public String getKeyName(InputAction action) {
        return actionToKey.get(action);
    }

    public InputAction getActionForKey(String keyName) {
        if (keyName == null) return null;
        return keyToAction.get(keyName.trim().toUpperCase());
    }

    public Map<String, String> toSerializableMap() {
        Map<String, String> snapshot = new HashMap<>();
        for (Map.Entry<InputAction, String> entry : actionToKey.entrySet()) {
            snapshot.put(entry.getKey().name(), entry.getValue());
        }
        return Collections.unmodifiableMap(snapshot);
    }
}