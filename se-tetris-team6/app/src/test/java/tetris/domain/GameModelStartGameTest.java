/**
 * 대상: tetris.domain.GameModel
 *
 * 목적:
 * - startGame 호출 시 모드/상태가 올바르게 설정되는지 스모크해 핵심 분기를 보강한다.
 *
 * 주요 시나리오:
 * 1) startGame(STANDARD) → currentMode/lastMode=STANDARD, 상태=PLAYING
 * 2) startGame(ITEM) → isItemMode=true, 상태=PLAYING
 */
package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.model.GameState;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;

class GameModelStartGameTest {

    private GameModel model;

    @BeforeEach
    void setUp() {
        FakeScoreRepo scoreRepo = new FakeScoreRepo();
        SettingService settingService = new SettingService(new FakeSettingRepo(), scoreRepo);
        model = new GameModel(new ConstantGenerator(BlockKind.I), scoreRepo, new FakeLeaderboardRepo(), settingService);
    }

    @Test
    void startGame_standard_setsModeAndState() {
        model.startGame(GameMode.STANDARD);
        assertEquals(GameMode.STANDARD, model.getCurrentMode());
        assertEquals(GameMode.STANDARD, model.getLastMode());
        assertEquals(GameState.PLAYING, model.getCurrentState());
    }

    @Test
    void startGame_item_setsItemMode() {
        model.startGame(GameMode.ITEM);
        assertEquals(GameMode.ITEM, model.getCurrentMode());
        assertTrue(model.isItemMode());
        assertEquals(GameState.PLAYING, model.getCurrentState());
    }

    // === 테스트용 간단한 협력자들 ===
    private static final class ConstantGenerator implements BlockGenerator {
        private final BlockKind kind;
        ConstantGenerator(BlockKind kind) { this.kind = kind; }
        @Override public BlockKind nextBlock() { return kind; }
    }

    private static final class FakeScoreRepo implements ScoreRepository {
        private Score score = Score.zero();
        @Override public Score load() { return score; }
        @Override public void save(Score score) { this.score = score; }
        @Override public void reset() { score = Score.zero(); }
    }

    private static final class FakeSettingRepo implements SettingRepository {
        private Setting setting = Setting.defaults();
        @Override public Setting load() { return setting; }
        @Override public void save(Setting setting) { this.setting = setting; }
        @Override public void resetToDefaults() { this.setting = Setting.defaults(); }
    }

    private static final class FakeLeaderboardRepo implements LeaderboardRepository {
        @Override public java.util.List<LeaderboardEntry> loadTop(int n, tetris.domain.GameMode mode) { return java.util.Collections.emptyList(); }
        @Override public void saveEntry(LeaderboardEntry entry) {}
        @Override public LeaderboardResult saveAndHighlight(LeaderboardEntry entry) { return new LeaderboardResult(java.util.Collections.emptyList(), -1); }
        @Override public void reset() {}
    }
}
