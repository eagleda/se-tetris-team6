package tetris;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.GameDifficulty;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.Setting.ScreenSize;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;

class SettingServiceTest {

    private FakeSettingRepository settingRepository;
    private FakeScoreRepository scoreRepository;
    private SettingService service;

    @BeforeEach
    void setUp() {
        settingRepository = new FakeSettingRepository(Setting.defaults());
        scoreRepository = new FakeScoreRepository();
        service = new SettingService(settingRepository, scoreRepository);
    }

    @Test
    void settersUpdateCachedSettings() {
        Map<String, Integer> bindings = new HashMap<>();
        bindings.put("MOVE_LEFT", KeyEvent.VK_A);
        bindings.put("MOVE_RIGHT", KeyEvent.VK_D);

        service.setScreenSize(ScreenSize.LARGE);
        service.setColorBlindMode(true);
        service.setDifficulty(GameDifficulty.HARD);
        service.setKeyBindings(bindings);

        Setting settings = service.getSettings();
        assertEquals(ScreenSize.LARGE, settings.getScreenSize());
        assertTrue(settings.isColorBlindMode());
        assertEquals(GameDifficulty.HARD, settings.getDifficulty());
        assertEquals(bindings, settings.getKeyBindings());

        bindings.put("ROTATE", KeyEvent.VK_W);
        assertFalse(settings.getKeyBindings().containsKey("ROTATE"),
            "key map should have been defensively copied");
    }

    @Test
    void resetScoreboardDelegatesToRepository() {
        assertFalse(scoreRepository.resetCalled);
        service.resetScoreboard();
        assertTrue(scoreRepository.resetCalled, "resetScoreboard should call ScoreRepository.reset()");
    }

    @Test
    void resetToDefaultsReplacesCachedSettingAndSaves() {
        service.setColorBlindMode(true);
        service.setDifficulty(GameDifficulty.EASY);
        service.setScreenSize(ScreenSize.SMALL);
        settingRepository.saved = null;

        service.resetToDefaults();

        Setting defaults = Setting.defaults();
        Setting current = service.getSettings();
        assertEquals(defaults.getScreenSize(), current.getScreenSize());
        assertEquals(defaults.isColorBlindMode(), current.isColorBlindMode());
        assertEquals(defaults.getDifficulty(), current.getDifficulty());
        assertNotNull(settingRepository.saved, "resetToDefaults should save defaults to repository");
    }

    @Test
    void savePersistsCurrentSettings() {
        service.setScreenSize(ScreenSize.SMALL);
        service.setColorBlindMode(true);
        service.setDifficulty(GameDifficulty.NORMAL);
        settingRepository.saved = null;

        service.save();

        assertNotNull(settingRepository.saved, "save should persist current cached setting");
        assertEquals(ScreenSize.SMALL, settingRepository.saved.getScreenSize());
        assertTrue(settingRepository.saved.isColorBlindMode());
    }

    private static final class FakeSettingRepository implements SettingRepository {
        private Setting stored;
        private Setting saved;

        FakeSettingRepository(Setting initial) {
            this.stored = copy(initial);
            this.saved = null;
        }

        @Override
        public Setting load() {
            return stored;
        }

        @Override
        public void save(Setting settings) {
            this.saved = copy(settings);
            this.stored = copy(settings);
        }

        @Override
        public void resetToDefaults() {
            stored = Setting.defaults();
        }

        private static Setting copy(Setting original) {
            Setting copy = Setting.defaults();
            copy.setScreenSize(original.getScreenSize());
            copy.setColorBlindMode(original.isColorBlindMode());
            copy.setDifficulty(original.getDifficulty());
            copy.setKeyBindings(new HashMap<>(original.getKeyBindings()));
            return copy;
        }
    }

    private static final class FakeScoreRepository implements ScoreRepository {
        private boolean resetCalled;
        private Score score = Score.zero();

        @Override
        public Score load() {
            return score;
        }

        @Override
        public void save(Score score) {
            this.score = score;
        }

        @Override
        public void reset() {
            resetCalled = true;
            score = Score.zero();
        }
    }

    // Stub to satisfy SettingService constructor when a LeaderboardRepository is needed elsewhere.
    static final class NoopLeaderboardRepository implements LeaderboardRepository {
        @Override
        public List<LeaderboardEntry> loadTop(int n, tetris.domain.GameMode mode) {
            return List.of();
        }

        @Override
        public void saveEntry(LeaderboardEntry entry) {}

        @Override
        public void reset() {}
    }
}
