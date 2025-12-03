/**
 * 대상: tetris.domain.setting.SettingService
 *
 * 목적:
 * - load/reset 흐름을 스모크하여 미싱 라인을 보강한다.
 */
package tetris.domain.setting;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tetris.domain.score.ScoreRepository;

class SettingServiceSmokeTest {

    @Test
    void load_and_reset_defaults() {
        SettingService service = new SettingService(new InMemorySettingRepo(), new InMemoryScoreRepo());
        assertNotNull(service.getSettings());
        service.resetToDefaults();
        assertNotNull(service.getSettings());
    }

    // 간단한 인메모리 구현
    static class InMemorySettingRepo implements SettingRepository {
        private Setting setting = Setting.defaults();
        @Override public Setting load() { return setting; }
        @Override public void save(Setting settings) { this.setting = settings; }
        @Override public void resetToDefaults() { this.setting = Setting.defaults(); }
    }
    static class InMemoryScoreRepo implements ScoreRepository {
        private tetris.domain.score.Score score = tetris.domain.score.Score.zero();
        @Override public tetris.domain.score.Score load() { return score; }
        @Override public void save(tetris.domain.score.Score score) { this.score = score; }
        @Override public void reset() { score = tetris.domain.score.Score.zero(); }
    }
}
