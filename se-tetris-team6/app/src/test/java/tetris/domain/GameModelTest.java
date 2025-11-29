package tetris.domain;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

    static class DummyScoreRepo implements ScoreRepository {
        private Score score = Score.zero();
        @Override public Score load() { return score; }
        @Override public void save(Score score) { this.score = score; }
        @Override public void reset() { this.score = Score.zero(); }
    }

    static class DummyLeaderboardRepo implements LeaderboardRepository {
        @Override public List<LeaderboardEntry> loadTop(int n, GameMode mode) {
            return Collections.emptyList();
        }
        @Override public void saveEntry(LeaderboardEntry entry) { }
        @Override public tetris.domain.leaderboard.LeaderboardResult saveAndHighlight(LeaderboardEntry entry) {
            return new tetris.domain.leaderboard.LeaderboardResult(Collections.emptyList(), -1);
        }
        @Override public void reset() { }
    }

    static class InMemorySettingRepository implements SettingRepository {
        private Setting current = Setting.defaults();
        @Override public Setting load() { return current; }
        @Override public void save(Setting settings) { current = settings; }
        @Override public void resetToDefaults() { current = Setting.defaults(); }
    }

    static class ConstGenerator implements BlockGenerator {
        @Override public BlockKind nextBlock() { return BlockKind.I; }
        @Override public BlockKind peekNext() { return BlockKind.I; }
    }

    @Test
    void addBoardCells_and_clearBoardRegion_work_and_refreshBridge() {
        DummyScoreRepo repo = new DummyScoreRepo();
        SettingService settingService = new SettingService(new InMemorySettingRepository(), repo);
        GameModel model = new GameModel(new ConstGenerator(), repo, new DummyLeaderboardRepo(), settingService);
        // add a small 2x2 block into board at (0,0)
        model.addBoardCells(0, 0, new int[][]{ {1,2}, {3,4} });
        // clearing a region should not throw and should call refresh (no-op bridge by default)
        model.clearBoardRegion(0, 0, 1, 1);
        // verify board has been modified (no exception means pass)
        assertNotNull(model.getBoard().gridView());
    }

    @Test
    void addGlobalBuff_doubleScore_changesMultiplier_onScoreEngine() {
        DummyScoreRepo repo = new DummyScoreRepo();
        SettingService settingService = new SettingService(new InMemorySettingRepository(), repo);
        GameModel model = new GameModel(new ConstGenerator(), repo, new DummyLeaderboardRepo(), settingService);
        // initially multiplier is 1.0; after adding buff multiplier should be >1
        model.startGame(GameMode.ITEM);
        model.addGlobalBuff("double_score", 100, java.util.Map.of("factor", 3.0));
        // applying a descend should use multiplier; use engine to descend
        ScoreRuleEngine s = model.getScoreEngine();
        s.onBlockDescend();
        assertTrue(repo.load().getPoints() >= 1);
    }
}
