/**
 * 대상: tetris.domain.GameModel 주요 분기 (스냅샷/스폰/버프/멀티훅)
 *
 * 목적:
 * - low coverage로 남은 toSnapshot(), onBlockSpawned(), rotateBlockClockwise(),
 *   notifyMultiplayerLineClear(), addGlobalBuff() 분기를 직접 호출해 커버리지를 높인다.
 * - 네트워크/멀티 훅 동작은 더미 구현으로 호출 여부만 확인한다.
 *
 * 주요 시나리오:
 * 1) toSnapshot(int): active/next 블록 정보와 보드/점수/모드가 채워지는지 확인
 * 2) toSnapshot(int, List): 공격 줄 목록이 스냅샷에 복사되는지 확인
 * 3) onBlockSpawned: ITEM 모드에서 nextBlockIsItem=true일 때 아이템 블록이 활성화되는지 확인
 * 4) rotateBlockClockwise: 회전 값이 0~3 범위에서 순환하는지 확인
 * 5) notifyMultiplayerLineClear: 마지막 잠금 스냅샷과 클리어 줄이 훅으로 전달되는지 확인
 * 6) addGlobalBuff: double_score/slow 버프가 만료 시각을 설정하는지 확인 (ITEM 모드 한정)
 */
package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.Board;
import tetris.domain.item.ItemBehavior;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.model.Block;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;
import tetris.multiplayer.model.AttackLine;
import tetris.multiplayer.model.Cell;
import tetris.multiplayer.model.LockedPieceSnapshot;

class GameModelAdditionalCoverageTest {

    private GameModel model;
    private FakeScoreRepo scoreRepo;

    @BeforeEach
    void setUp() {
        scoreRepo = new FakeScoreRepo();
        SettingService settingService = new SettingService(new FakeSettingRepo(), scoreRepo);
        model = new GameModel(new ConstantGenerator(BlockKind.I), scoreRepo, new FakeLeaderboardRepo(), settingService);
        model.startGame(GameMode.STANDARD);
    }

    @Test
    void toSnapshot_basicFieldsPopulated() {
        model.getBoard().setCell(0, 0, 7);
        Block active = Block.spawn(BlockKind.O, 2, 3);
        active.setRotation(1);
        model.setActiveBlock(active);

        var snapshot = model.toSnapshot(2);

        assertNotNull(snapshot.board());
        assertTrue(snapshot.currentBlockId() > 0);
        assertTrue(snapshot.nextBlockId() >= 0);
        assertEquals(2, snapshot.playerId());
        assertTrue(snapshot.blockRotation() >= 0 && snapshot.blockRotation() < 4);
    }

    @Test
    void toSnapshot_withAttackLinesCopiesHoles() {
        List<AttackLine> lines = List.of(new AttackLine(new boolean[] {true, false, true}));
        var snapshot = model.toSnapshot(1, lines);
        assertNotNull(snapshot.attackLines());
        assertEquals(1, snapshot.attackLines().length);
        assertEquals(3, snapshot.attackLines()[0].length);
    }

    @Test
    void onBlockSpawned_itemMode_setsActiveItemBlock() throws Exception {
        // ITEM 모드로 전환 및 아이템 스폰 강제
        setField(model, "currentMode", GameMode.ITEM);
        setField(model, "nextBlockIsItem", true);
        // 행동 오버라이드로 랜덤성 제거
        java.util.function.Supplier<ItemBehavior> supplier = tetris.domain.item.behavior.DoubleScoreBehavior::new;
        setField(model, "behaviorOverride", supplier);

        Block block = Block.spawn(BlockKind.I, 0, 0);
        Method m = GameModel.class.getMethod("onBlockSpawned", Block.class);
        m.invoke(model, block);

        Field activeItemField = GameModel.class.getDeclaredField("activeItemBlock");
        activeItemField.setAccessible(true);
        Object itemBlock = activeItemField.get(model);
        assertNotNull(itemBlock);
    }

    @Test
    void rotateBlockClockwise_wrapsRotation() {
        Block block = Block.spawn(BlockKind.I, 0, 0);
        block.setRotation(3);
        model.setActiveBlock(block);

        model.rotateBlockClockwise();

        assertEquals(0, model.getActiveBlock().getRotation());
    }

    @Test
    void notifyMultiplayerLineClear_invokesHookWithRows() throws Exception {
        // gameplayEngine 모킹: 마지막 클리어 줄 반환
        tetris.domain.engine.GameplayEngine engine = org.mockito.Mockito.mock(tetris.domain.engine.GameplayEngine.class);
        org.mockito.Mockito.when(engine.getLastClearedRows()).thenReturn(List.of(1, 3));
        setField(model, "gameplayEngine", engine);

        // 마지막 잠금 스냅샷 세팅
        LockedPieceSnapshot snapshot = LockedPieceSnapshot.of(List.of(new Cell(0, 0)));
        setField(model, "lastLockedPieceSnapshot", snapshot);

        class Hook implements GameModel.MultiplayerHook {
            int called = 0;
            int[] lastRows;
            @Override public void onPieceLocked(LockedPieceSnapshot s, int[] clearedRows, int boardWidth) {
                called++;
                lastRows = clearedRows;
                assertEquals(Board.W, boardWidth);
            }
            @Override public void beforeNextSpawn() {}
        }
        Hook hook = new Hook();
        model.addMultiplayerHook(hook);

        Method m = GameModel.class.getDeclaredMethod("notifyMultiplayerLineClear");
        m.setAccessible(true);
        m.invoke(model);

        assertEquals(1, hook.called);
        assertNotNull(hook.lastRows);
        assertEquals(2, hook.lastRows.length);
    }

    @Test
    void addGlobalBuff_setsExpireTimes() throws Exception {
        setField(model, "currentMode", GameMode.ITEM);

        model.addGlobalBuff("double_score", 0L, Map.of("durationMs", 50L, "factor", 3.0));
        Field expiresField = GameModel.class.getDeclaredField("doubleScoreBuffExpiresAtMs");
        expiresField.setAccessible(true);
        long expires = expiresField.getLong(model);
        assertTrue(expires > 0);

        model.addGlobalBuff("slow", 0L, Map.of("durationMs", 25L, "levelDelta", -2));
        Field slowExpiresField = GameModel.class.getDeclaredField("slowBuffExpiresAtMs");
        slowExpiresField.setAccessible(true);
        long slowExpires = slowExpiresField.getLong(model);
        assertTrue(slowExpires > 0);
    }

    // ==== 테스트용 협력자 및 유틸 ====
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
        @Override public java.util.List<LeaderboardEntry> loadTop(int n, GameMode mode) { return new ArrayList<>(); }
        @Override public void saveEntry(LeaderboardEntry entry) { }
        @Override public LeaderboardResult saveAndHighlight(LeaderboardEntry entry) { return new LeaderboardResult(java.util.Collections.emptyList(), -1); }
        @Override public void reset() { }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = GameModel.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
