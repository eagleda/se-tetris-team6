package tetris.domain.setting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tetris.domain.score.ScoreRepository;

class SettingServiceTest {

    static class InMemoryScoreRepo implements ScoreRepository {
        private tetris.domain.score.Score score = tetris.domain.score.Score.zero();
        @Override public tetris.domain.score.Score load() { return score; }
        @Override public void save(tetris.domain.score.Score score) { this.score = score; }
        @Override public void reset() { this.score = tetris.domain.score.Score.zero(); }
    }

    static class InMemorySettingRepo implements SettingRepository {
        private Setting setting = Setting.defaults();
        @Override public Setting load() { return setting; }
        @Override public void save(Setting settings) { setting = settings; }
        @Override public void resetToDefaults() { setting = Setting.defaults(); }
    }

    @Test
    void resetScoreboard_delegatesToScoreRepository() {
        InMemoryScoreRepo scoreRepo = new InMemoryScoreRepo();
        SettingService service = new SettingService(new InMemorySettingRepo(), scoreRepo);
        scoreRepo.save(tetris.domain.score.Score.of(100));

        service.resetScoreboard();

        assertEquals(0, scoreRepo.load().getPoints());
    }
}
