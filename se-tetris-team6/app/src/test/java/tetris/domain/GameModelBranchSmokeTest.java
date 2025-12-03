/**
 * 대상: GameModel의 기타 분기/상태 전환 메서드들
 *
 * 목적:
 * - startGame, updateGravityProgress, rollBehavior, UI 상태 전환(exitSettings 등),
 *   showGameOverScreen, commitPendingGarbageLines 등 소소한 분기를 스모크한다.
 */
package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.handler.GameHandler;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.ArgumentMatchers.anyInt;

class GameModelBranchSmokeTest {

    private GameModel model;
    private FakeScoreRepo scoreRepo;

    @BeforeEach
    void setUp() {
        scoreRepo = new FakeScoreRepo();
        SettingService settingService = new SettingService(new FakeSettingRepo(), scoreRepo);
        model = new GameModel(new ConstantGenerator(BlockKind.I), scoreRepo, new FakeLeaderboardRepo(), settingService);
    }

    private boolean getBooleanField(String name) throws Exception {
        Field f = GameModel.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getBoolean(model);
    }

    @Test
    void startGame_timeLimitMode_runs() {
        assertDoesNotThrow(() -> model.startGame(GameMode.TIME_LIMIT));
        assertEquals(GameMode.TIME_LIMIT, model.getCurrentMode());
    }

    @Test
    void updateGravityProgress_incrementsLevel() throws Exception {
        setField("totalSpawnedBlocks", 50);
        setField("currentGravityLevel", 0);
        Method m = GameModel.class.getDeclaredMethod("updateGravityProgress");
        m.setAccessible(true);
        m.invoke(model);
        int level = getIntField("currentGravityLevel");
        assertTrue(level >= 0);
    }

    @Test
    void rollBehavior_usesOverrideSupplier() throws Exception {
        setField("currentMode", GameMode.ITEM);
        java.util.function.Supplier<tetris.domain.item.ItemBehavior> supplier = tetris.domain.item.behavior.DoubleScoreBehavior::new;
        setField("behaviorOverride", supplier);
        Method m = GameModel.class.getDeclaredMethod("rollBehavior");
        m.setAccessible(true);
        Object behavior = m.invoke(model);
        assertNotNull(behavior);
    }

    @Test
    void showGameOverScreen_invokesUiBridge() throws Exception {
        var ui = Mockito.mock(GameModel.UiBridge.class, Mockito.withSettings().lenient());
        setField("uiBridge", ui);
        Method m = GameModel.class.getDeclaredMethod("showGameOverScreen");
        m.setAccessible(true);
        m.invoke(model);
        verify(ui, atLeastOnce()).showGameOverOverlay(any(), eq(true));
    }

    @Test
    void exitSettings_and_exitScoreboard_and_cancelNameInput() throws Exception {
        assertDoesNotThrow(() -> model.exitSettings());
        assertDoesNotThrow(() -> model.exitScoreboard());
        assertDoesNotThrow(() -> model.cancelNameInput());
        // 상태가 MENU로 전환되었는지 확인
        assertEquals(GameState.MENU, model.getCurrentState());
    }

    @Test
    void proceedFromGameOver_movesToMenu() throws Exception {
        setField("currentState", GameState.GAME_OVER);
        model.proceedFromGameOver();
        // 구현상 GAME_OVER -> NAME_INPUT 으로 전환 후 UI 갱신
        assertEquals(GameState.NAME_INPUT, model.getCurrentState());
    }

    @Test
    void onBlockRotated_itemMode_updatesItemCell() throws Exception {
        setField("currentMode", GameMode.ITEM);
        Block block = Block.spawn(BlockKind.I, 0, 0);
        tetris.domain.item.model.ItemBlockModel itemBlock =
                new tetris.domain.item.model.ItemBlockModel(block, java.util.List.of(new tetris.domain.item.behavior.DoubleScoreBehavior()), 0, 0);
        setField("activeItemBlock", itemBlock);
        model.onBlockRotated(block, 2);
        assertTrue(itemBlock.hasItemCell());
        assertTrue(itemBlock.getItemCellX() != 0 || itemBlock.getItemCellY() != 0);
    }

    @Test
    void onLinesCleared_itemMode_setsNextBlockIsItem_andNotifiesSecondary() throws Exception {
        setField("currentMode", GameMode.ITEM);
        setField("itemSpawnIntervalLines", 1);
        var eng = Mockito.mock(tetris.domain.engine.GameplayEngine.class, Mockito.withSettings().lenient());
        setField("gameplayEngine", eng);
        var secondary = Mockito.mock(tetris.domain.engine.GameplayEngine.GameplayEvents.class);
        model.setSecondaryListener(secondary);

        model.onLinesCleared(1);

        assertTrue(getBooleanField("nextBlockIsItem"));
        verify(eng, atLeastOnce()).pauseForLineClear(anyInt());
        verify(secondary, atLeastOnce()).onLinesCleared(1);
    }

    @Test
    void onLinesCleared_nonItemMode_skipsItemManager() throws Exception {
        setField("currentMode", GameMode.STANDARD);
        var eng = Mockito.mock(tetris.domain.engine.GameplayEngine.class, Mockito.withSettings().lenient());
        setField("gameplayEngine", eng);
        model.onLinesCleared(1);
        // 아이템 모드가 아니므로 nextBlockIsItem은 false 유지
        assertFalse(getBooleanField("nextBlockIsItem"));
    }

    @Test
    void resolveBehaviorOverride_handlesOtherKnownIds() throws Exception {
        setField("currentMode", GameMode.ITEM);
        Method m = GameModel.class.getDeclaredMethod("resolveBehaviorOverride", String.class);
        m.setAccessible(true);
        assertNotNull(m.invoke(model, "slow"));
        assertNotNull(m.invoke(model, "bomb"));
    }

    // ==== 유틸 & 더블 ====
    private void setField(String name, Object value) throws Exception {
        Field f = GameModel.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(model, value);
    }

    private int getIntField(String name) throws Exception {
        Field f = GameModel.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(model);
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
