package tetris;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tetris.data.setting.PreferencesSettingRepository;
import tetris.domain.setting.Setting;

class PreferencesSettingRepositoryTest {

    @Test
    void saveAndLoadRoundtrip() {
    java.util.UUID uuid = java.util.UUID.randomUUID();
    String tempNode = "se-tetris-team6/test/" + uuid.toString();
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node(tempNode);
    PreferencesSettingRepository repo = new PreferencesSettingRepository(prefs);
    Setting s = new Setting();
        s.setScreenSize(Setting.ScreenSize.LARGE);
        s.setColorBlindMode(true);
        Map<String, Integer> kb = new HashMap<>();
        kb.put("MOVE_LEFT", java.awt.event.KeyEvent.VK_A);
        kb.put("MOVE_RIGHT", java.awt.event.KeyEvent.VK_D);
        kb.put("ROTATE", java.awt.event.KeyEvent.VK_W);
        kb.put("SOFT_DROP", java.awt.event.KeyEvent.VK_S);
        kb.put("HARD_DROP", java.awt.event.KeyEvent.VK_SPACE);
        kb.put("HOLD", java.awt.event.KeyEvent.VK_C);
        s.setKeyBindings(kb);

        try {
            repo.save(s);

            Setting loaded = repo.load();
            assertEquals(Setting.ScreenSize.LARGE, loaded.getScreenSize());
            assertTrue(loaded.isColorBlindMode());
            assertEquals(java.awt.event.KeyEvent.VK_A, loaded.getKeyBinding("MOVE_LEFT").intValue());
            assertEquals(java.awt.event.KeyEvent.VK_D, loaded.getKeyBinding("MOVE_RIGHT").intValue());
        } finally {
            // cleanup test node
            try {
                prefs.removeNode();
            } catch (Exception ex) {
                // ignore cleanup failures in test
            }
        }
        
    }
}
