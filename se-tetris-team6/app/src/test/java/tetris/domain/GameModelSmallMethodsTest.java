/**
 * 대상: GameModel의 단순 위임/상태 전환 메서드들
 *
 * 목적:
 * - 회전/이동 위임, 멀티 세션 enable/clear, 버프·아이템 설정 등
 *   짧은 분기들을 직접 호출해 커버리지를 보강한다.
 */
package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.model.Block;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;
import tetris.multiplayer.session.LocalMultiplayerSession;
import tetris.multiplayer.session.NetworkMultiplayerSession;

class GameModelSmallMethodsTest {

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
    void rotateBlockCounterClockwise_wrapsToThreeFromZero() {
        Block b = Block.spawn(BlockKind.I, 0, 0);
        b.setRotation(0);
        model.setActiveBlock(b);
        model.rotateBlockCounterClockwise();
        assertEquals(3, model.getActiveBlock().getRotation());
    }

    @Test
    void movementMethods_delegateToGameplayEngine() throws Exception {
        var engine = Mockito.mock(tetris.domain.engine.GameplayEngine.class);
        // 활성 블록이 있다고 인식시키기 위해 반환값 스텁
        engine = Mockito.mock(tetris.domain.engine.GameplayEngine.class, Mockito.withSettings().lenient());
        Mockito.when(engine.getActiveBlock()).thenReturn(Block.spawn(BlockKind.I, 0, 0));
        setField("gameplayEngine", engine);

        model.moveBlockLeft();
        model.moveBlockRight();
        model.moveBlockDown();
        model.hardDropBlock();
        model.holdCurrentBlock();

        verify(engine, atLeastOnce()).moveBlockLeft();
        verify(engine, atLeastOnce()).moveBlockRight();
        verify(engine, atLeastOnce()).moveBlockDown();
        verify(engine, atLeastOnce()).hardDropBlock();
        verify(engine, atLeastOnce()).holdCurrentBlock();
    }

    @Test
    void setItemBehaviorOverride_setsBehaviorSupplier() throws Exception {
        // ITEM 모드에서 오버라이드 설정 시 supplier가 채워지는지 확인
        setField("currentMode", GameMode.ITEM);
        model.setItemBehaviorOverride("double_score");
        assertNotNull(getField("behaviorOverride"));
    }

    @Test
    void setItemBehaviorOverride_blankResetsSupplier() throws Exception {
        setField("currentMode", GameMode.ITEM);
        model.setItemBehaviorOverride("double_score");
        model.setItemBehaviorOverride("  "); // blank -> reset
        assertEquals(null, getField("behaviorOverride"));
    }

    @Test
    void enableAndClearMultiplayerSessions_resetsPresence() {
        LocalMultiplayerSession local = mock(LocalMultiplayerSession.class);
        model.enableLocalMultiplayer(local);
        assertTrue(model.getActiveLocalMultiplayerSession().isPresent());

        NetworkMultiplayerSession net = mock(NetworkMultiplayerSession.class);
        model.enableNetworkMultiplayer(net);
        assertTrue(model.getActiveNetworkMultiplayerSession().isPresent());

        model.clearLocalMultiplayerSession(); // 내부에서 모든 세션을 정리하는 private clearMultiplayerSessions 호출
        assertTrue(model.getActiveLocalMultiplayerSession().isEmpty());
        assertTrue(model.getActiveNetworkMultiplayerSession().isEmpty());
    }

    @Test
    void onLinesCleared_increasesScoreOrStaysNonNegative() {
        long before = scoreRepo.load().getPoints();
        model.onLinesCleared(2);
        long after = scoreRepo.load().getPoints();
        assertTrue(after >= before);
    }

    @Test
    void getSlowBuffRemainingTimeMs_positiveWhenNotExpired() throws Exception {
        setField("slowBuffExpiresAtMs", System.currentTimeMillis() + 1_000);
        assertTrue(model.getSlowBuffRemainingTimeMs() > 0);
    }

    @Test
    void setItemSpawnIntervalLines_acceptsPositive_andRejectsNonPositive() {
        model.setItemSpawnIntervalLines(5);
        assertThrows(IllegalArgumentException.class, () -> model.setItemSpawnIntervalLines(0));
        assertThrows(IllegalArgumentException.class, () -> model.setItemSpawnIntervalLines(-1));
    }

    @Test
    void commitPendingGarbageLines_clearsCounterAndRefreshesUi() throws Exception {
        setField("pendingGarbageLines", 3);
        var ui = Mockito.mock(tetris.domain.GameModel.UiBridge.class);
        setField("uiBridge", ui);
        model.commitPendingGarbageLines();
        verify(ui, atLeastOnce()).refreshBoard();
        assertEquals(0, getIntField("pendingGarbageLines"));
    }

    @Test
    void notifyBeforeNextSpawnHooks_invokesHook() throws Exception {
        class Hook implements GameModel.MultiplayerHook {
            int called = 0;
            @Override public void onPieceLocked(tetris.multiplayer.model.LockedPieceSnapshot s, int[] rows, int w) {}
            @Override public void beforeNextSpawn() { called++; }
        }
        Hook hook = new Hook();
        model.addMultiplayerHook(hook);
        Method m = GameModel.class.getDeclaredMethod("notifyBeforeNextSpawnHooks");
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(model));
        assertEquals(1, hook.called);
        model.removeMultiplayerHook(hook);
    }

    @Test
    void forceNextBlockAsItem_setsFlag() throws Exception {
        setField("currentMode", GameMode.ITEM);
        assertFalse(getBooleanField("nextBlockIsItem"));
        model.forceNextBlockAsItem();
        assertTrue(getBooleanField("nextBlockIsItem"));
    }

    @Test
    void getSlowBuffRemainingTimeMs_zeroWhenExpired() throws Exception {
        setField("slowBuffExpiresAtMs", System.currentTimeMillis() - 1000);
        assertEquals(0, model.getSlowBuffRemainingTimeMs());
    }

    @Test
    void commitPendingGarbageLines_noopWhenZero() {
        assertDoesNotThrow(() -> model.commitPendingGarbageLines());
    }

    @Test
    void getItemSpawnIntervalLines_reflectsSetter() {
        model.setItemSpawnIntervalLines(3);
        assertEquals(3, model.getItemSpawnIntervalLines());
    }

    @Test
    void movementMethods_noopWhenNotPlaying() throws Exception {
        var engine = Mockito.mock(tetris.domain.engine.GameplayEngine.class, Mockito.withSettings().lenient());
        setField("gameplayEngine", engine);
        setField("currentState", tetris.domain.model.GameState.PAUSED);

        model.moveBlockLeft();
        model.moveBlockRight();
        model.moveBlockDown();
        model.hardDropBlock();
        model.holdCurrentBlock();

        verify(engine, never()).moveBlockLeft();
        verify(engine, never()).moveBlockRight();
        verify(engine, never()).moveBlockDown();
        verify(engine, never()).hardDropBlock();
        verify(engine, never()).holdCurrentBlock();
    }

    @Test
    void resolveBehaviorOverride_otherIdsReturnSupplier() throws Exception {
        setField("currentMode", GameMode.ITEM);
        Method m = GameModel.class.getDeclaredMethod("resolveBehaviorOverride", String.class);
        m.setAccessible(true);
        assertNotNull(m.invoke(model, "weight"));
        assertNotNull(m.invoke(model, "double_score"));
    }

    // ===== 유틸/테스트 더블 =====
    private void setField(String name, Object value) throws Exception {
        Field f = GameModel.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(model, value);
    }

    private Object getField(String name) throws Exception {
        Field f = GameModel.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(model);
    }

    private int getIntField(String name) throws Exception {
        Field f = GameModel.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(model);
    }

    private boolean getBooleanField(String name) throws Exception {
        Field f = GameModel.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getBoolean(model);
    }

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
        @Override public java.util.List<LeaderboardEntry> loadTop(int n, GameMode mode) { return java.util.List.of(); }
        @Override public void saveEntry(LeaderboardEntry entry) { }
        @Override public LeaderboardResult saveAndHighlight(LeaderboardEntry entry) { return new LeaderboardResult(java.util.List.of(), -1); }
        @Override public void reset() { }
    }
}
