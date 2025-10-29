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
    private final Preferences prefs = Preferences.userRoot().node(NODE);

    // keys
    private static final String KEY_SCREEN = "screenSize";
    private static final String KEY_COLORBLIND = "colorBlind";
    private static final String KEY_PREFIX_KB = "kb."; // e.g. kb.MOVE_LEFT

    private static final String[] ACTIONS = { "MOVE_LEFT", "MOVE_RIGHT", "ROTATE", "SOFT_DROP", "HARD_DROP", "HOLD" };

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

        Map<String, String> kb = new HashMap<>();
        for (String a : ACTIONS) {
            String v = prefs.get(KEY_PREFIX_KB + a, null);
            if (v == null) {
                v = Setting.defaults().getKeyBinding(a);
            }
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
        for (Map.Entry<String, String> e : settings.getKeyBindings().entrySet()) {
            prefs.put(KEY_PREFIX_KB + e.getKey(), e.getValue());
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
