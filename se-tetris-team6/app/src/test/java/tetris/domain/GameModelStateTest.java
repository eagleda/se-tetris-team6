package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.model.GameState;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;

/*
 * 테스트 대상: tetris.domain.GameModel
 *
 * 역할 요약:
 * - 상태 전환, 설정/색각 모드 반영, 리더보드 조회 등의 도메인 조정자.
 *
 * 테스트 전략:
 * - startGame/pauseGame/resumeGame/quitToMenu 상태 변화를 검증.
 * - 리더보드 조회 실패 시 빈 리스트로 대체되는지 확인.
 * - 색각 모드 토글이 내부 플래그에 반영되는지 확인.
 */
class GameModelStateTest {

    private ScoreRepository scoreRepo;
    private LeaderboardRepository leaderboardRepo;
    private SettingService settingService;

    @BeforeEach
    void setUp() {
        scoreRepo = new GameModelTest.DummyScoreRepo();
        leaderboardRepo = mock(LeaderboardRepository.class);
        settingService = new SettingService(new GameModelTest.InMemorySettingRepository(), scoreRepo);
    }

    private GameModel newModel() {
        return new GameModel(new GameModelTest.ConstGenerator(), scoreRepo, leaderboardRepo, settingService);
    }

    @Test
    void startPauseResumeQuit_changesState() {
        GameModel model = newModel();

        model.startGame(GameMode.STANDARD);
        assertEquals(GameState.PLAYING, model.getCurrentState());
        assertEquals(GameMode.STANDARD, model.getCurrentMode());

        model.pauseGame();
        assertEquals(GameState.PAUSED, model.getCurrentState());

        model.resumeGame();
        assertEquals(GameState.PLAYING, model.getCurrentState());

        model.quitToMenu();
        assertEquals(GameState.MENU, model.getCurrentState());
    }

    @Test
    void loadTopScores_handlesRepositoryException() {
        GameModel model = newModel();
        doThrow(new RuntimeException("fail")).when(leaderboardRepo).loadTop(Mockito.anyInt(), Mockito.any());

        List<LeaderboardEntry> entries = model.loadTopScores(GameMode.STANDARD, 5);

        assertTrue(entries.isEmpty());
    }

    @Test
    void setColorBlindMode_updatesFlag() {
        GameModel model = newModel();
        assertFalse(model.isColorBlindMode());

        model.setColorBlindMode(true);

        assertTrue(model.isColorBlindMode());
        // SettingService 내부 상태도 true로 변경되었는지 확인
        Setting settings = settingService.getSettings();
        assertTrue(settings.isColorBlindMode());
    }
}
