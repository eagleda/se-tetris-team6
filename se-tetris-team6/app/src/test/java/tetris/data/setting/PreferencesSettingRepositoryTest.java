package tetris.data.setting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tetris.domain.setting.Setting;

class PreferencesSettingRepositoryTest {

    @Test
    void saveAndLoadRoundtrip() {
        String tempNode = "se-tetris-team6/test/" + java.util.UUID.randomUUID();
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node(tempNode);
        PreferencesSettingRepository repo = new PreferencesSettingRepository(prefs);

        Setting setting = Setting.defaults();
        Map<String, Integer> keys = new HashMap<>();
        keys.put("MOVE_LEFT", java.awt.event.KeyEvent.VK_J);
        setting.setKeyBindings(keys);
        setting.setColorBlindMode(true);
        setting.setScreenSize(Setting.ScreenSize.LARGE);

        repo.save(setting);
        Setting loaded = repo.load();

        assertEquals(Setting.ScreenSize.LARGE, loaded.getScreenSize());
        assertTrue(loaded.isColorBlindMode());
        assertEquals(java.awt.event.KeyEvent.VK_J, loaded.getKeyBinding("MOVE_LEFT"));
    }
}
