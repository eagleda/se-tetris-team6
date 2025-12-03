package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;
import tetris.network.protocol.AttackLine;

/*
 * 테스트 대상: GameModel.applyAttackLines
 *
 * 역할 요약:
 * - 네트워크로 수신된 공격 라인의 강도를 누적해 pendingGarbageLines에 저장하고,
 *   10줄을 넘지 않도록 캡(cap)하며 UI 갱신을 요청한다.
 *
 * 테스트 전략:
 * - 공격 줄을 여러 번 적용해 대기열이 누적되고 10줄로 제한되는지 검증.
 * - 이미 10줄일 때 추가 공격이 무시되어 UI 갱신이 발생하지 않는지 확인.
 */
class GameModelAttackLinesTest {

    private GameModel model;
    private RefreshRecorder ui;

    @BeforeEach
    void setUp() {
        SettingService settingService = new SettingService(new FakeSettingRepo(), new FakeScoreRepo());
        model = new GameModel(new ConstantGenerator(BlockKind.I), new FakeScoreRepo(), new FakeLeaderboardRepo(), settingService);
        ui = new RefreshRecorder();
        model.bindUiBridge(ui);
    }

    @Test
    void applyAttackLines_accumulatesAndCapsAtTen() throws Exception {
        model.applyAttackLines(new AttackLine[] { new AttackLine(3), new AttackLine(4) }); // -> 7
        model.applyAttackLines(new AttackLine[] { new AttackLine(5) }); // 7+5 -> capped 10

        assertEquals(10, getPendingGarbage(model));
        assertEquals(2, ui.refreshCount); // 두 번 모두 UI 갱신 호출
    }

    @Test
    void applyAttackLines_ignoredWhenAlreadyFull() throws Exception {
        setPendingGarbage(model, 10);
        model.applyAttackLines(new AttackLine[] { new AttackLine(3) });

        assertEquals(10, getPendingGarbage(model)); // 그대로 유지
        assertEquals(0, ui.refreshCount); // 새 갱신 없음
    }

    // === 테스트 보조 클래스들 ===
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
        private Setting s = Setting.defaults();
        @Override public Setting load() { return s; }
        @Override public void save(Setting settings) { this.s = settings; }
        @Override public void resetToDefaults() { this.s = Setting.defaults(); }
    }

    private static class FakeLeaderboardRepo implements LeaderboardRepository {
        @Override public java.util.List<LeaderboardEntry> loadTop(int n, GameMode mode) { return java.util.Collections.emptyList(); }
        @Override public void saveEntry(LeaderboardEntry entry) {}
        @Override public LeaderboardResult saveAndHighlight(LeaderboardEntry entry) { return new LeaderboardResult(java.util.List.of(), -1); }
        @Override public void reset() {}
    }

    private static class RefreshRecorder implements GameModel.UiBridge {
        int refreshCount = 0;
        @Override public void showPauseOverlay() {}
        @Override public void hidePauseOverlay() {}
        @Override public void refreshBoard() { refreshCount++; }
        @Override public void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName) {}
        @Override public void showNameEntryOverlay(tetris.domain.score.Score score) {}
    }

    private int getPendingGarbage(GameModel m) throws Exception {
        Field f = GameModel.class.getDeclaredField("pendingGarbageLines");
        f.setAccessible(true);
        return (int) f.get(m);
    }

    private void setPendingGarbage(GameModel m, int value) throws Exception {
        Field f = GameModel.class.getDeclaredField("pendingGarbageLines");
        f.setAccessible(true);
        f.set(m, value);
    }
}
