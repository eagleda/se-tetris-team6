package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.data.setting.PreferencesSettingRepository;
import tetris.domain.model.GameState;
import tetris.domain.setting.SettingService;
import tetris.multiplayer.session.LocalMultiplayerSession;
import tetris.multiplayer.session.NetworkMultiplayerSession;

/**
 * GameModel의 자잘한 퍼블릭/패키지 메서드를 호출해 남은 분기를 보강한다.
 */
class GameModelMiscSmallBranchesTest {

    private GameModel model;
    private CountingBridge bridge;

    @BeforeEach
    void setUp() {
        var scoreRepo = new InMemoryScoreRepository();
        var lbRepo = new InMemoryLeaderboardRepository();
        var settingService = new SettingService(new PreferencesSettingRepository(), scoreRepo);
        model = new GameModel(new RandomBlockGenerator(), scoreRepo, lbRepo, settingService);
        model.changeState(GameState.PLAYING);
        bridge = new CountingBridge();
        model.bindUiBridge(bridge);
    }

    @Test
    void settingsAndScoreboardNavigation_refreshesUi() {
        model.changeState(GameState.SETTINGS);
        assertDoesNotThrow(() -> model.exitSettings());
        assertDoesNotThrow(() -> model.resetAllSettings());
        assertDoesNotThrow(() -> model.navigateSettingsUp());
        assertDoesNotThrow(() -> model.navigateSettingsDown());
        assertDoesNotThrow(() -> model.selectCurrentSetting());

        model.changeState(GameState.SCOREBOARD);
        assertDoesNotThrow(() -> model.exitScoreboard());
        assertDoesNotThrow(() -> model.scrollScoreboardUp());
        assertDoesNotThrow(() -> model.scrollScoreboardDown());

        assertTrue(bridge.refreshCount > 0);
    }

    @Test
    void gameOverAndNameInput_callUiBridge() {
        model.showPauseOverlay();
        model.hidePauseOverlay();
        assertEquals(1, bridge.showPauseCount);
        assertEquals(1, bridge.hidePauseCount);

        model.changeState(GameState.GAME_OVER);
        assertDoesNotThrow(() -> model.proceedFromGameOver());
        model.changeState(GameState.NAME_INPUT);
        assertDoesNotThrow(() -> model.cancelNameInput());
        assertDoesNotThrow(() -> model.confirmNameInput());
    }

    @Test
    void clearMultiplayerSessions_shutsDownSessions() throws Exception {
        LocalMultiplayerSession local = mock(LocalMultiplayerSession.class);
        NetworkMultiplayerSession net = mock(NetworkMultiplayerSession.class);
        setField("activeLocalSession", local);
        setField("activeNetworkSession", net);
        Method clear = GameModel.class.getDeclaredMethod("clearMultiplayerSessions");
        clear.setAccessible(true);
        clear.invoke(model);
        verify(local).shutdown();
        verify(net).shutdown();
    }

    @Test
    void buffTimersAndGarbageHelpers_doNotThrow() throws Exception {
        assertEquals(0, model.getSlowBuffRemainingTimeMs());
        assertEquals(0, model.getDoubleScoreBuffRemainingTimeMs());
        model.setItemSpawnIntervalLines(8);
        model.setItemBehaviorOverride("line_clear");
        setField("pendingGarbageLines", 1);
        model.commitPendingGarbageLines();
        int pending = (int) getField("pendingGarbageLines");
        assertEquals(0, pending);
    }

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

    private static class CountingBridge implements GameModel.UiBridge {
        int showPauseCount, hidePauseCount, refreshCount;
        @Override public void showPauseOverlay() { showPauseCount++; }
        @Override public void hidePauseOverlay() { hidePauseCount++; }
        @Override public void refreshBoard() { refreshCount++; }
        @Override public void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName) {}
        @Override public void showNameEntryOverlay(tetris.domain.score.Score score) {}
    }
}
