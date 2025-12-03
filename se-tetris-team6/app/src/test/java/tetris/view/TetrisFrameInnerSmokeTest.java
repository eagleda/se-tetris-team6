/**
 * 대상: tetris.view.TetrisFrame의 다양한 익명 내부 클래스($2$1, $4, $10, $11 등)
 *
 * 목적:
 * - 네트워크 상태 타이머 액션, cleanupNetworkSession, GameOverPanel 버튼 리스너를 리플렉션으로 호출해
 *   0% 커버리지 inner 리스너들을 스모크한다.
 *
 * 주요 시나리오:
 * 1) networkUpdateTimer 액션 리스너 직접 호출 시 예외 없는지 확인
 * 2) cleanupNetworkSession을 리플렉션으로 호출해 quit/세션 정리 경로를 스모크
 * 3) GameOverPanel의 skip/back 버튼 클릭이 예외 없이 동작하는지 확인
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.GameModel;
import tetris.domain.score.ScoreRuleEngine;
import tetris.view.GameComponent.GameOverPanel;

class TetrisFrameInnerSmokeTest {

    private GameModel model;
    private TetrisFrame frame;

    @BeforeEach
    void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 UI 테스트 스킵");
        model = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        var repo = new InMemoryScoreRepository();
        Mockito.when(model.getScoreRepository()).thenReturn(repo);
        Mockito.when(model.getScoreEngine()).thenReturn(new ScoreRuleEngine(repo));
        Mockito.when(model.getLeaderboardRepository()).thenReturn(new InMemoryLeaderboardRepository());
        frame = new TetrisFrame(model);
        frame.setVisible(false);
    }

    @AfterEach
    void tearDown() {
        if (frame != null) frame.dispose();
    }

    @Test
    void networkTimer_and_cleanup_noThrow() throws Exception {
        var f = TetrisFrame.class.getDeclaredField("networkUpdateTimer");
        f.setAccessible(true);
        javax.swing.Timer timer = (javax.swing.Timer) f.get(frame);
        if (timer != null && timer.getActionListeners().length > 0) {
            assertDoesNotThrow(() -> timer.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, "tick")));
        }
        var m = TetrisFrame.class.getDeclaredMethod("cleanupNetworkSession");
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(frame));
    }

    @Test
    void gameOverPanelButtons_noThrow() throws Exception {
        var panelField = TetrisFrame.class.getDeclaredField("gameOverPanel");
        panelField.setAccessible(true);
        GameOverPanel panel = (GameOverPanel) panelField.get(frame);

        var skipF = GameOverPanel.class.getDeclaredField("skipButton");
        var backF = GameOverPanel.class.getDeclaredField("backToMenu");
        skipF.setAccessible(true);
        backF.setAccessible(true);
        ((javax.swing.JButton) skipF.get(panel)).doClick();
        ((javax.swing.JButton) backF.get(panel)).doClick();
    }
}
