/**
 * 대상: tetris.view.TetrisFrame의 익명 내부 클래스들 ($11 네트워크 타이머, $12 GameOverPanel 리스너)
 *
 * 목적:
 * - 네트워크 상태 타이머 액션과 GameOverPanel 리스너(onSkip/onBackToMenu) 경로를 호출하여 0% 커버리지를 보강한다.
 * - Mockito 사용 이유: 실제 GameModel/Leaderboard/ScoreRepository를 대체해 UI 생성 의존성을 최소화하기 위함.
 *
 * 주요 시나리오:
 * 1) networkUpdateTimer의 ActionListener를 직접 호출해 예외 없이 실행되는지 확인
 * 2) GameOverPanel의 리스너를 통해 onSkip/onBackToMenu 호출 시 quitToMenu가 호출되는지 확인
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.GameModel;
import tetris.domain.score.ScoreRuleEngine;
import tetris.view.GameComponent.GameOverPanel;

class TetrisFrameTimerAndGameOverTest {

    private GameModel model;
    private TetrisFrame frame;

    @BeforeEach
    void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 UI 테스트 스킵");
        model = mock(GameModel.class, org.mockito.Mockito.withSettings().lenient());
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
    void networkTimer_actionExecutes() {
        try {
            java.lang.reflect.Field f = TetrisFrame.class.getDeclaredField("networkUpdateTimer");
            f.setAccessible(true);
            javax.swing.Timer timer = (javax.swing.Timer) f.get(frame);
            if (timer != null && timer.getActionListeners().length > 0) {
                assertDoesNotThrow(() -> timer.getActionListeners()[0].actionPerformed(null));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void gameOverPanelListener_callsQuitToMenu() {
        try {
            java.lang.reflect.Field panelField = TetrisFrame.class.getDeclaredField("gameOverPanel");
            panelField.setAccessible(true);
            GameOverPanel panel = (GameOverPanel) panelField.get(frame);

            // reflection to click skip/back buttons
            java.lang.reflect.Field skipF = GameOverPanel.class.getDeclaredField("skipButton");
            java.lang.reflect.Field backF = GameOverPanel.class.getDeclaredField("backToMenu");
            skipF.setAccessible(true);
            backF.setAccessible(true);
            javax.swing.JButton skip = (javax.swing.JButton) skipF.get(panel);
            javax.swing.JButton back = (javax.swing.JButton) backF.get(panel);

            skip.doClick();
            back.doClick();

            verify(model, atLeastOnce()).quitToMenu();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
