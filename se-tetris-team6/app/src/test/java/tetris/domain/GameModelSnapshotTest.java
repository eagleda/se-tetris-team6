package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.model.Block;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;
import tetris.network.protocol.GameSnapshot;

/*
 * 테스트 대상: GameModel.applySnapshot (내부 applySnapshotImpl까지 포함)
 *
 * 역할 요약:
 * - 네트워크에서 받은 GameSnapshot을 보드/블록/점수/공격 대기열/클리어 라인 상태에 반영한다.
 *
 * 테스트 전략:
 * - 간단한 스냅샷을 적용한 뒤 보드/액티브 블록/점수/클리어 라인/공격 대기열이 동기화됐는지 확인.
 * - EDT에서 실행되도록 invokeAndWait로 동기화.
 */
class GameModelSnapshotTest {

    private GameModel model;
    private FakeScoreRepo scoreRepo;

    @BeforeEach
    void setUp() {
        scoreRepo = new FakeScoreRepo();
        SettingService settingService = new SettingService(new FakeSettingRepo(), scoreRepo);
        model = new GameModel(new ConstantGenerator(BlockKind.I), scoreRepo, new FakeLeaderboardRepo(), settingService);
    }

    @Test
    void applySnapshot_appliesBoardBlockScoreAndLines() throws Exception {
        int[][] board = {
                {1, 2},
                {3, 4}
        };
        boolean[][] attack = { { true, false } };
        int[] cleared = { 1 };
        GameSnapshot snapshot = new GameSnapshot(
                1, board,
                /*current*/1, /*next*/2,
                /*score*/500, /*elapsedSeconds*/3,
                /*pendingGarbage*/2,
                /*blockX*/4, /*blockY*/5, /*rot*/2,
                attack,
                "STANDARD",
                null, -1, -1,
                cleared
        );

        SwingUtilities.invokeAndWait(() -> model.applySnapshot(snapshot));

        // 보드 셀 복원
        int[][] view = model.getBoard().gridView();
        assertEquals(1, view[0][0]);
        assertEquals(4, view[1][1]);

        // 액티브 블록 위치/회전
        Block active = model.getActiveBlock();
        assertNotNull(active);
        assertEquals(4, active.getX());
        assertEquals(5, active.getY());
        assertEquals(2, active.getRotation());

        // 점수 동기화
        assertEquals(500, scoreRepo.load().getPoints());

        // 클리어 라인/공격 대기열 동기화
        List<Integer> lastCleared = model.getLastClearedLines();
        assertEquals(1, lastCleared.size());
        assertEquals(1, lastCleared.get(0));
        assertEquals(1, model.getSnapshotAttackLines().size());
    }

    /* 테스트용 최소 구현들 */
    private static class ConstantGenerator implements BlockGenerator {
        private final BlockKind kind;
        ConstantGenerator(BlockKind kind) { this.kind = kind; }
        @Override public BlockKind nextBlock() { return kind; }
        @Override public BlockKind peekNext() { return kind; }
    }

    private static class FakeScoreRepo implements ScoreRepository {
        private Score score = Score.zero();
        @Override public Score load() { return score; }
        @Override public void save(Score score) { this.score = score; }
        @Override public void reset() { score = Score.zero(); }
    }

    private static class FakeSettingRepo implements SettingRepository {
        private Setting setting = Setting.defaults();
        @Override public Setting load() { return setting; }
        @Override public void save(Setting settings) { this.setting = settings; }
        @Override public void resetToDefaults() { this.setting = Setting.defaults(); }
    }

    private static class FakeLeaderboardRepo implements LeaderboardRepository {
        @Override public List<LeaderboardEntry> loadTop(int n, GameMode mode) { return Collections.emptyList(); }
        @Override public void saveEntry(LeaderboardEntry entry) { }
        @Override public LeaderboardResult saveAndHighlight(LeaderboardEntry entry) { return new LeaderboardResult(List.of(), -1); }
        @Override public void reset() { }
    }
}
