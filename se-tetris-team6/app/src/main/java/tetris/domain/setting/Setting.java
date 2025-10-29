package tetris.domain.setting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple settings model representing UI and control preferences.
 */
public class Setting {

    public enum ScreenSize {
        SMALL, MEDIUM, LARGE
    }

    private ScreenSize screenSize;
    private Map<String, String> keyBindings;
    private boolean colorBlindMode;

    public Setting() {
        this.keyBindings = new HashMap<>();
    }

    public static Setting defaults() {
        Setting s = new Setting();
        s.screenSize = ScreenSize.MEDIUM;
        s.colorBlindMode = false;
        Map<String, String> kb = new HashMap<>();
        kb.put("MOVE_LEFT", "LEFT");
        kb.put("MOVE_RIGHT", "RIGHT");
        kb.put("ROTATE", "UP");
        kb.put("SOFT_DROP", "DOWN");
        kb.put("HARD_DROP", "SPACE");
        kb.put("HOLD", "C");
        s.keyBindings = kb;
        return s;
    }

    public ScreenSize getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(ScreenSize screenSize) {
        this.screenSize = screenSize;
    }

    public Map<String, String> getKeyBindings() {
        return Collections.unmodifiableMap(keyBindings);
    }

    public void setKeyBindings(Map<String, String> keyBindings) {
        this.keyBindings = new HashMap<>(keyBindings);
    }

    public String getKeyBinding(String action) {
        return keyBindings.get(action);
    }

    public void setKeyBinding(String action, String keyName) {
        this.keyBindings.put(action, keyName);
    }

    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    public void setColorBlindMode(boolean colorBlindMode) {
        this.colorBlindMode = colorBlindMode;
    }
}
