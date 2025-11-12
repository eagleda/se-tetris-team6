package tetris.domain.setting;

import java.awt.Dimension;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import tetris.domain.GameDifficulty;

/**
 * Simple settings model representing UI and control preferences.
 */
public class Setting {

    public enum ScreenSize {
        SMALL(new Dimension(560, 720)),
        MEDIUM(new Dimension(700, 900)),
        LARGE(new Dimension(780, 1000));

        private final Dimension dimension;

        ScreenSize(Dimension d) {
            this.dimension = d;
        }

        public Dimension getDimension() {
            return new Dimension(dimension);
        }
    }

    private ScreenSize screenSize;
    private Map<String, Integer> keyBindings;
    private boolean colorBlindMode;
    private GameDifficulty difficulty;

    public Setting() {
        this.keyBindings = new HashMap<>();
    }

    public static Setting defaults() {
        Setting s = new Setting();
        s.screenSize = ScreenSize.MEDIUM;
        s.colorBlindMode = false;
        s.difficulty = GameDifficulty.NORMAL;
        Map<String, Integer> kb = new HashMap<>();
        kb.put("MOVE_LEFT", java.awt.event.KeyEvent.VK_LEFT);
        kb.put("MOVE_RIGHT", java.awt.event.KeyEvent.VK_RIGHT);
        kb.put("ROTATE_CW", java.awt.event.KeyEvent.VK_UP);
        kb.put("SOFT_DROP", java.awt.event.KeyEvent.VK_DOWN);
        kb.put("HARD_DROP", java.awt.event.KeyEvent.VK_SPACE);
        kb.put("HOLD", java.awt.event.KeyEvent.VK_C);
        s.keyBindings = kb;
        return s;
    }

    public ScreenSize getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(ScreenSize screenSize) {
        this.screenSize = screenSize;
    }

    public Map<String, Integer> getKeyBindings() {
        return Collections.unmodifiableMap(keyBindings);
    }

    public void setKeyBindings(Map<String, Integer> keyBindings) {
        this.keyBindings = new HashMap<>(keyBindings);
    }

    public Integer getKeyBinding(String action) {
        return keyBindings.get(action);
    }

    public void setKeyBinding(String action, Integer keyCode) {
        this.keyBindings.put(action, keyCode);
    }

    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    public void setColorBlindMode(boolean colorBlindMode) {
        this.colorBlindMode = colorBlindMode;
    }

    public GameDifficulty getDifficulty() {
        return difficulty != null ? difficulty : GameDifficulty.NORMAL;
    }

    public void setDifficulty(GameDifficulty difficulty) {
        this.difficulty = difficulty != null ? difficulty : GameDifficulty.NORMAL;
    }
}
