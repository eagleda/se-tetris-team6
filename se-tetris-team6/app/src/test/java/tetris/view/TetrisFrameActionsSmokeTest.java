/**
 * 대상: tetris.view.TetrisFrame의 전역 키 액션들 ($1~$5)
 *
 * 목적:
 * - ActionMap에 등록된 전역 키 바인딩(togglePausePanel, goMainPanel, moveUpButton,
 *   moveDownButton, clickFocusButton)이 예외 없이 동작하고,
 *   pause/resume/quitToMenu 호출 여부를 검증하여 0% 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) togglePausePanel: PLAYING -> pauseGame, PAUSED -> resumeGame 호출
 * 2) goMainPanel: quitToMenu 호출
 * 3) moveUp/moveDown/clickFocus: 예외 없이 실행
 *
 * Mockito 사용 이유:
 * - TetrisFrame 생성 시 필요한 GameModel/Repository/Leaderboard 의존성을 간단히 스텁하기 위함.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.domain.score.ScoreRuleEngine;

class TetrisFrameActionsSmokeTest {

    private GameModel model;
    private TetrisFrame frame;

    @BeforeEach
    void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 UI 테스트 스킵");
        model = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        var repo = new InMemoryScoreRepository();
        when(model.getScoreRepository()).thenReturn(repo);
        when(model.getScoreEngine()).thenReturn(new ScoreRuleEngine(repo));
        when(model.getLeaderboardRepository()).thenReturn(new InMemoryLeaderboardRepository());
        frame = new TetrisFrame(model);
        frame.setVisible(false);
    }

    @AfterEach
    void tearDown() {
        if (frame != null) frame.dispose();
    }

    @Test
    void togglePauseAndGoMain_invokeModelMethods() {
        when(model.getCurrentState()).thenReturn(GameState.PLAYING, GameState.PAUSED);
        Action toggle = frame.getRootPane().getActionMap().get("togglePausePanel");
        toggle.actionPerformed(new ActionEvent(this, 0, "esc"));
        toggle.actionPerformed(new ActionEvent(this, 0, "esc"));
        verify(model, times(1)).pauseGame();
        verify(model, times(1)).resumeGame();

        Action goMain = frame.getRootPane().getActionMap().get("goMainPanel");
        goMain.actionPerformed(new ActionEvent(this, 0, "home"));
        verify(model, times(1)).quitToMenu();
    }

    @Test
    void moveAndClickActions_executeSafely() {
        Action up = frame.getRootPane().getActionMap().get("moveUpButton");
        Action down = frame.getRootPane().getActionMap().get("moveDownButton");
        Action click = frame.getRootPane().getActionMap().get("clickFocusButton");

        assertDoesNotThrow(() -> up.actionPerformed(new ActionEvent(this, 0, "up")));
        assertDoesNotThrow(() -> down.actionPerformed(new ActionEvent(this, 0, "down")));
        assertDoesNotThrow(() -> click.actionPerformed(new ActionEvent(this, 0, "enter")));
    }
}
