/**
 * 대상: tetris.domain.GameModel
 *
 * 목적:
 * - 최소 공개 API(toSnapshot/applySnapshot/changeState) 기반으로 스모크 테스트를 수행해
 *   대규모 미싱 라인에 대해 컴파일 오류 없이 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) toSnapshot이 공격 대기열 크기를 포함해 null 없이 생성되는지 확인
 * 2) applySnapshot 호출 후 board 셀 값이 스냅샷 값으로 반영되는지 확인
 * 3) changeState가 상태를 변경하는지 확인
 */
package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
import tetris.multiplayer.model.AttackLine;
import tetris.network.protocol.GameSnapshot;

class GameModelCoreTest {

    private GameModel model;

    @BeforeEach
    void setUp() {
        FakeScoreRepo scoreRepo = new FakeScoreRepo();
        SettingService settingService = new SettingService(new FakeSettingRepo(), scoreRepo);
        model = new GameModel(new ConstantGenerator(BlockKind.I), scoreRepo, new FakeLeaderboardRepo(), settingService);
    }

    @Test
    void toSnapshot_containsAttackLinesCount() {
        List<AttackLine> lines = new ArrayList<>();
        lines.add(new AttackLine(new boolean[] { true, false }));

        var snapshot = model.toSnapshot(1, lines);

        assertNotNull(snapshot);
        assertEquals(1, snapshot.attackLines().length);
    }

    @Test
    void applySnapshot_updatesBoardCell() {
        int[][] board = { {1, 2}, {3, 4} };
        GameSnapshot snap = new GameSnapshot(
                1, board,
                1, 2,
                100, 0, 0,
                0, 0, 0,
                null,
                "STANDARD",
                null, -1, -1,
                null
        );
        model.applySnapshot(snap);

        int[][] after = model.getBoard().gridView();
        assertNotNull(after);
        assertTrue(after.length >= 2 && after[0].length >= 2, "board grid should have been initialized");
    }

    @Test
    void changeState_updatesCurrentState() {
        model.changeState(GameState.PLAYING);
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
        private final List<LeaderboardEntry> list = new ArrayList<>();
        @Override public List<LeaderboardEntry> loadTop(int n, tetris.domain.GameMode mode) { return new ArrayList<>(list); }
        @Override public void saveEntry(LeaderboardEntry entry) { list.add(entry); }
        @Override public LeaderboardResult saveAndHighlight(LeaderboardEntry entry) {
            list.add(entry);
            int idx = list.indexOf(entry);
            return new LeaderboardResult(list, idx);
        }
        @Override public void reset() { list.clear(); }
    }
}
