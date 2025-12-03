/*
 * 테스트 대상: tetris.view.TetrisFrame의 익명 AbstractAction들 (TetrisFrame$1~$5)
 *
 * 역할 요약:
 * - ESC/Home/방향키/Enter 전역 키 바인딩을 처리하여 일시정지/메뉴 이동/포커스 이동/버튼 클릭을 수행합니다.
 *
 * 테스트 전략:
 * - Mockito로 GameModel을 스텁하여 Frame 생성 시 필요한 의존성을 주입합니다.
 * - ActionMap에서 각 액션을 직접 호출하여 pause/resume/quitToMenu가 호출되는지 확인하고,
 *   포커스 이동/클릭 액션은 예외 없이 실행되는지만 검증합니다.
 *
 * 주요 테스트 시나리오 예시:
 * - togglePausePanel: PLAYING → pauseGame, PAUSED → resumeGame 호출
 * - goMainPanel: quitToMenu 호출
 * - moveUp/moveDown/clickFocus: 예외 없이 동작
 */

package tetris.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Optional;

import javax.swing.Action;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.controller.GameController;
import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.domain.score.ScoreRuleEngine;

class TetrisFrameActionsTest {

    private GameModel model;
    private TetrisFrame frame;

    @BeforeEach
    void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skip UI actions in headless environments");
        model = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        // 필수 의존성 스텁
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        when(model.getScoreRepository()).thenReturn(repo);
        when(model.getScoreEngine()).thenReturn(new ScoreRuleEngine(repo));
        when(model.getLeaderboardRepository()).thenReturn(new InMemoryLeaderboardRepository());
        when(model.getActiveNetworkMultiplayerSession()).thenReturn(Optional.empty());
        when(model.getActiveLocalMultiplayerSession()).thenReturn(Optional.empty());
        when(model.loadTopScores(GameMode.STANDARD, 10)).thenReturn(Collections.emptyList());
        when(model.loadTopScores(GameMode.ITEM, 10)).thenReturn(Collections.emptyList());

        frame = new TetrisFrame(model);
        // 테스트 시 보이지 않게 처리
        frame.setVisible(false);
    }

    @AfterEach
    void tearDown() {
        if (frame != null) {
            frame.dispose();
        }
    }

    @Test
    void togglePausePanel_and_goMainPanel_actions_invokeModelMethods() {
        when(model.getCurrentState()).thenReturn(GameState.PLAYING, GameState.PAUSED);

        Action toggle = frame.getRootPane().getActionMap().get("togglePausePanel");
        toggle.actionPerformed(new ActionEvent(this, 0, "toggle"));
        toggle.actionPerformed(new ActionEvent(this, 0, "toggle"));

        verify(model, times(1)).pauseGame();
        verify(model, times(1)).resumeGame();

        Action goMain = frame.getRootPane().getActionMap().get("goMainPanel");
        goMain.actionPerformed(new ActionEvent(this, 0, "home"));
        verify(model, times(1)).quitToMenu();
    }

    @Test
    void moveAndClickActions_executeWithoutException() {
        Action up = frame.getRootPane().getActionMap().get("moveUpButton");
        Action down = frame.getRootPane().getActionMap().get("moveDownButton");
        Action click = frame.getRootPane().getActionMap().get("clickFocusButton");

        assertDoesNotThrow(() -> up.actionPerformed(new ActionEvent(this, 0, "up")));
        assertDoesNotThrow(() -> down.actionPerformed(new ActionEvent(this, 0, "down")));
        assertDoesNotThrow(() -> click.actionPerformed(new ActionEvent(this, 0, "enter")));
    }
}
