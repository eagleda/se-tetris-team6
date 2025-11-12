package tetris.data.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;

/**
 * Simple Preferences-based implementation of SettingRepository.
 * Uses java.util.prefs.Preferences to persist primitive values; keys are stored
 * under node /se-tetris-team6/settings.
 */
public class PreferencesSettingRepository implements SettingRepository {

    private static final String NODE = "se-tetris-team6/settings";
    private Preferences prefs;

    public PreferencesSettingRepository() {
        this(Preferences.userRoot().node(NODE));
    }

    public PreferencesSettingRepository(Preferences prefs) {
        this.prefs = prefs;
    }

    // keys
    private static final String KEY_SCREEN = "screenSize";
    private static final String KEY_COLORBLIND = "colorBlind";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_PREFIX_KB = "kb."; // e.g. kb.MOVE_LEFT

    private static final String[] ACTIONS = { "MOVE_LEFT", "MOVE_RIGHT", "ROTATE_CW", "SOFT_DROP", "HARD_DROP", "HOLD" };

    @Override
    public Setting load() {
        Setting s = new Setting();
        String screen = prefs.get(KEY_SCREEN, null);
        if (screen != null) {
            try {
                s.setScreenSize(Setting.ScreenSize.valueOf(screen));
            } catch (IllegalArgumentException e) {
                s.setScreenSize(Setting.defaults().getScreenSize());
            }
        } else {
            s.setScreenSize(Setting.defaults().getScreenSize());
        }

        s.setColorBlindMode(prefs.getBoolean(KEY_COLORBLIND, Setting.defaults().isColorBlindMode()));

        String difficulty = prefs.get(KEY_DIFFICULTY, Setting.defaults().getDifficulty().name());
        try {
            s.setDifficulty(tetris.domain.GameDifficulty.valueOf(difficulty));
        } catch (IllegalArgumentException e) {
            s.setDifficulty(Setting.defaults().getDifficulty());
        }

        Map<String, Integer> kb = new HashMap<>();
        for (String a : ACTIONS) {
            int defaultCode = Setting.defaults().getKeyBinding(a);
            int v = prefs.getInt(KEY_PREFIX_KB + a, defaultCode);
            kb.put(a, v);
        }
        s.setKeyBindings(kb);
        return s;
    }

    @Override
    public void save(Setting settings) {
        if (settings.getScreenSize() != null) {
            prefs.put(KEY_SCREEN, settings.getScreenSize().name());
        }
        prefs.putBoolean(KEY_COLORBLIND, settings.isColorBlindMode());
        if (settings.getDifficulty() != null) {
            prefs.put(KEY_DIFFICULTY, settings.getDifficulty().name());
        }
        for (Map.Entry<String, Integer> e : settings.getKeyBindings().entrySet()) {
            prefs.putInt(KEY_PREFIX_KB + e.getKey(), e.getValue());
        }
        try {
            prefs.flush();
        } catch (Exception ex) {
            // best-effort persistence; swallow but could log
        }
    }

    @Override
    public void resetToDefaults() {
        Setting d = Setting.defaults();
        save(d);
    }
}
