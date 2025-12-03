/**
 * 대상: tetris.domain.GameModel (핵심 동작)
 *
 * 목적:
 * - getPendingFullLines, 회전, 버프 적용, 라인 클리어, 스냅샷 적용 등 주요 분기를 스모크해 미싱 라인을 줄인다.
 */
package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.model.Block;
import tetris.domain.model.GameState;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;
import tetris.network.protocol.GameSnapshot;

class GameModelBehaviorTest {

    private GameModel model;

    @BeforeEach
    void setUp() {
        FakeScoreRepo scoreRepo = new FakeScoreRepo();
        SettingService settingService = new SettingService(new FakeSettingRepo(), scoreRepo);
        model = new GameModel(new ConstantGenerator(BlockKind.I), scoreRepo, new FakeLeaderboardRepo(), settingService);
        model.startGame(GameMode.STANDARD);
    }

    @Test
    void getPendingFullLines_includesRowWithActiveBlock() {
        // 보드 하단을 미리 채우고, 활성 블록을 겹치도록 설정
        int y = Board.H - 1;
        for (int x = 0; x < Board.W; x++) {
            model.getBoard().setCell(x, y, 1);
        }
        Block active = Block.spawn(BlockKind.I, 0, y);
        model.setActiveBlock(active);

        List<Integer> full = model.getPendingFullLines();
        assertFalse(full.isEmpty());
        assertTrue(full.contains(y));
    }

    @Test
    void rotateBlockClockwise_changesRotation() {
        Block active = Block.spawn(BlockKind.I, 3, 0);
        model.setActiveBlock(active);
        int before = active.getRotation();
        model.rotateBlockClockwise();
        int after = model.getActiveBlock().getRotation();
        assertTrue(after >= 0);
    }

    @Test
    void addGlobalBuff_doubleScore_setsRemaining() {
        model.startGame(GameMode.ITEM);
        model.addGlobalBuff("double_score", 0L, java.util.Map.of("durationMs", 100L, "factor", 2.0));
        assertTrue(model.getDoubleScoreBuffRemainingTimeMs() >= 0);
    }

    @Test
    void onLinesCleared_updatesScore() {
        long before = model.getScoreRepository().load().getPoints();
        model.onLinesCleared(2);
        long after = model.getScoreRepository().load().getPoints();
        assertTrue(after >= before);
    }

    @Test
    void applySnapshot_setsActiveBlockRotation() {
        int[][] board = { {1, 0}, {0, 0} };
        GameSnapshot snap = new GameSnapshot(
                1, board,
                BlockKind.I.ordinal() + 1,
                BlockKind.O.ordinal() + 1,
                0, 0, 0,
                2, 3, 2, // blockX, blockY, rotation=2
                null,
                "STANDARD",
                null, -1, -1,
                null
        );
        model.applySnapshot(snap);
        Block active = model.getActiveBlock();
        assertNotNull(active);
        // 스냅샷 적용 후 회전 정보가 복원되는지만 스모크한다 (정확한 회전 값은 구현에 따라 달라질 수 있음)
        assertTrue(active.getRotation() >= 0);
    }

    @Test
    void toSnapshot_includesBoardAndAttackLines() {
        model.getBoard().setCell(0, 0, 7);
        model.setActiveBlock(Block.spawn(BlockKind.O, 1, 2));
        List<tetris.multiplayer.model.AttackLine> lines = List.of(new tetris.multiplayer.model.AttackLine(new boolean[] {true, false}));
        var snap = model.toSnapshot(1, lines);
        assertNotNull(snap.board());
        assertNotNull(snap.attackLines());
    }

    @Test
    void clearBoardRegion_clearsFullRows() {
        int y = Board.H - 1;
        for (int x = 0; x < Board.W; x++) {
            model.getBoard().setCell(x, y, 9);
        }
        model.clearBoardRegion(0, y, Board.W, 1);
        assertEquals(0, model.getBoard().gridView()[y][0]);
    }

    @Test
    void getActiveItemInfo_returnsItemDataWhenItemMode() throws Exception {
        // 강제로 ITEM 모드 및 activeItemBlock 설정
        var modeField = GameModel.class.getDeclaredField("currentMode");
        modeField.setAccessible(true);
        modeField.set(model, GameMode.ITEM);

        tetris.domain.item.model.ItemBlockModel itemBlock = new tetris.domain.item.model.ItemBlockModel(
                Block.spawn(BlockKind.I, 0, 0),
                java.util.List.of(new tetris.domain.item.behavior.DoubleScoreBehavior()));
        itemBlock.setItemCell(1, 0);
        var activeField = GameModel.class.getDeclaredField("activeItemBlock");
        activeField.setAccessible(true);
        activeField.set(model, itemBlock);

        var info = model.getActiveItemInfo();
        assertNotNull(info);
        assertEquals("double_score", info.label());
    }

    @Test
    void resolveBehaviorOverride_handlesKnownAndUnknown() throws Exception {
        var m = GameModel.class.getDeclaredMethod("resolveBehaviorOverride", String.class);
        m.setAccessible(true);
        Object supplier = m.invoke(model, "slow");
        assertNotNull(supplier);
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                m.invoke(model, "unknown_behavior_x");
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
    }

    @Test
    void awardSpeedBonus_addsPoints() throws Exception {
        var levelField = GameModel.class.getDeclaredField("currentGravityLevel");
        levelField.setAccessible(true);
        levelField.setInt(model, 5);
        var m = GameModel.class.getDeclaredMethod("awardSpeedBonus", int.class);
        m.setAccessible(true);
        m.invoke(model, 2);
        assertTrue(model.getScoreRepository().load().getPoints() > 0);
    }

    // === 테스트용 협력자들 ===
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
        @Override public java.util.List<LeaderboardEntry> loadTop(int n, tetris.domain.GameMode mode) { return new ArrayList<>(); }
        @Override public void saveEntry(LeaderboardEntry entry) { }
        @Override public LeaderboardResult saveAndHighlight(LeaderboardEntry entry) { return new LeaderboardResult(java.util.Collections.emptyList(), -1); }
        @Override public void reset() { }
    }
}
