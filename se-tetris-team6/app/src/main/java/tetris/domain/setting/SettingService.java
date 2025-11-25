package tetris.domain.setting;

import java.util.HashMap;
import java.util.Map;

import tetris.domain.GameDifficulty;
import tetris.domain.score.ScoreRepository;

/**
 * Domain service to manage settings and related operations (eg. resetting scoreboard).
 */
public class SettingService {

    private final SettingRepository repository;
    private final ScoreRepository scoreRepository;
    private Setting cached;

    public SettingService(SettingRepository repository, ScoreRepository scoreRepository) {
        this.repository = repository;
        this.scoreRepository = scoreRepository;
        this.cached = repository.load();
    }

    public Setting getSettings() {
        if (cached == null) {
            cached = repository.load();
        }
        return cached;
    }

    public void setScreenSize(Setting.ScreenSize size) {
        getSettings().setScreenSize(size);
    }

    public void setColorBlindMode(boolean enabled) {
        getSettings().setColorBlindMode(enabled);
    }

    public void setDifficulty(GameDifficulty difficulty) {
        getSettings().setDifficulty(difficulty);
    }

    public void setKeyBinding(String action, String keyName) {
        // accept key code as stringified int or name? For now assume string is name and map externally
        // keep method for compatibility but not used elsewhere
    }

    public void reload() {
        cached = repository.load();
    }

    public void resetToDefaults() {
        Setting d = Setting.defaults();
        cached = d;
        repository.save(d);
    }

    public void resetScoreboard() {
        if (scoreRepository != null) {
            scoreRepository.reset();
        }
    }

    public void save() {
        if (cached != null) {
            repository.save(cached);
        }
    }

    /**
     * Convenience: replace all key bindings at once.
     */
    public void setKeyBindings(Map<String, Integer> map) {
        Map<String, Integer> m = new HashMap<>(map);
        getSettings().setKeyBindings(m);
    }
}
