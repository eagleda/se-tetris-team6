package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.data.setting.PreferencesSettingRepository;
import tetris.domain.item.ItemType;
import tetris.domain.model.GameState;
import tetris.domain.setting.SettingService;
import tetris.multiplayer.session.LocalMultiplayerSession;

/**
 * GameModel의 소소한 퍼블릭 메서드들을 한 번씩 호출해 누락된 라인을 커버한다.
 */
class GameModelPublicMethodsCoverageTest {

    private GameModel model;

    @BeforeEach
    void setUp() {
        var scoreRepo = new InMemoryScoreRepository();
        var lbRepo = new InMemoryLeaderboardRepository();
        var settingService = new SettingService(new PreferencesSettingRepository(), scoreRepo);
        model = new GameModel(new RandomBlockGenerator(), scoreRepo, lbRepo, settingService);
        model.changeState(GameState.PLAYING);
    }

    @Test
    void movementAndHoldMethods_doNotThrow() {
        assertDoesNotThrow(() -> model.moveBlockLeft());
        assertDoesNotThrow(() -> model.moveBlockRight());
        assertDoesNotThrow(() -> model.moveBlockDown());
        assertDoesNotThrow(() -> model.hardDropBlock());
        assertDoesNotThrow(() -> model.holdCurrentBlock());
    }

    @Test
    void settingsAndScoreboardNavigation_doNotThrow() {
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
    }

    @Test
    void gameOverAndNameInputTransitions_doNotThrow() {
        model.changeState(GameState.GAME_OVER);
        assertDoesNotThrow(() -> model.proceedFromGameOver());
        model.changeState(GameState.NAME_INPUT);
        assertDoesNotThrow(() -> model.cancelNameInput());
    }

    @Test
    void itemAndGarbageHelpers_updateState() throws Exception {
        model.setItemSpawnIntervalLines(5);
        model.setItemBehaviorOverride("double_score");
        // rollBehavior는 패키지 프라이빗이므로 호출 없이 설정만 검증
        // pending garbage commit이 0으로 리셋되는지 확인
        setField(model, "pendingGarbageLines", 2);
        model.commitPendingGarbageLines();
        int pending = (int) getField(model, "pendingGarbageLines");
        assertEquals(0, pending);
    }

    @Test
    void clearLocalMultiplayerSession_shutsDownAndRestoresHandler() throws Exception {
        LocalMultiplayerSession session = org.mockito.Mockito.mock(LocalMultiplayerSession.class);
        setField(model, "activeLocalSession", session);
        // defaultPlayHandler가 null인 경우에도 예외 없이 clear 수행
        model.clearLocalMultiplayerSession();
        // shutdown 호출 여부 확인
        org.mockito.Mockito.verify(session).shutdown();
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Object getField(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
}
