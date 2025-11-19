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
        SMALL(new Dimension(1120, 720)),
        MEDIUM(new Dimension(1400, 900)),
        LARGE(new Dimension(1560, 1000));

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
        // Single-player defaults
        kb.put("MOVE_LEFT", java.awt.event.KeyEvent.VK_LEFT);
        kb.put("MOVE_RIGHT", java.awt.event.KeyEvent.VK_RIGHT);
        kb.put("ROTATE_CW", java.awt.event.KeyEvent.VK_UP);
        kb.put("SOFT_DROP", java.awt.event.KeyEvent.VK_DOWN);
        kb.put("HARD_DROP", java.awt.event.KeyEvent.VK_SPACE);
        kb.put("HOLD", java.awt.event.KeyEvent.VK_C);
        // Multiplayer Player 1 defaults
        kb.put("P1_MOVE_LEFT", java.awt.event.KeyEvent.VK_W);
        kb.put("P1_MOVE_RIGHT", java.awt.event.KeyEvent.VK_D);
        kb.put("P1_ROTATE_CW", java.awt.event.KeyEvent.VK_W);
        kb.put("P1_SOFT_DROP", java.awt.event.KeyEvent.VK_S);
        kb.put("P1_HARD_DROP", java.awt.event.KeyEvent.VK_SPACE);
        // Multiplayer Player 2 defaults 
        kb.put("P2_MOVE_LEFT", java.awt.event.KeyEvent.VK_LEFT);
        kb.put("P2_MOVE_RIGHT", java.awt.event.KeyEvent.VK_RIGHT);
        kb.put("P2_ROTATE_CW", java.awt.event.KeyEvent.VK_UP);
        kb.put("P2_SOFT_DROP", java.awt.event.KeyEvent.VK_DOWN);
        kb.put("P2_HARD_DROP", java.awt.event.KeyEvent.VK_ENTER);
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
