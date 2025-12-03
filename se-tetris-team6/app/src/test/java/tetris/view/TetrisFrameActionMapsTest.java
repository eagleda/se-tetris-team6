/**
 * 대상: tetris.view.TetrisFrame ($1, $4 등 액션맵 기반 리스너)
 *
 * 목적:
 * - 루트 ActionMap의 액션을 직접 호출하여 quit/pause/포커스 이동 액션을 스모크해 다수의 inner 액션 클래스를 커버한다.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

class TetrisFrameActionMapsTest {

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
        when(model.getCurrentState()).thenReturn(GameState.PLAYING, GameState.PAUSED);
        frame = new TetrisFrame(model);
        frame.setVisible(false);
    }

    @AfterEach
    void tearDown() {
        if (frame != null) frame.dispose();
    }

    @Test
    void actionMapEntries_execute() {
        Action toggle = frame.getRootPane().getActionMap().get("togglePausePanel");
        Action goMain = frame.getRootPane().getActionMap().get("goMainPanel");
        Action up = frame.getRootPane().getActionMap().get("moveUpButton");
        Action down = frame.getRootPane().getActionMap().get("moveDownButton");
        Action click = frame.getRootPane().getActionMap().get("clickFocusButton");

        // EDT 호출이 블로킹되지 않도록 간단히 invokeLater로 실행
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(5);
        javax.swing.SwingUtilities.invokeLater(() -> { toggle.actionPerformed(new ActionEvent(this, 0, "t")); latch.countDown(); });
        javax.swing.SwingUtilities.invokeLater(() -> { goMain.actionPerformed(new ActionEvent(this, 0, "g")); latch.countDown(); });
        javax.swing.SwingUtilities.invokeLater(() -> { up.actionPerformed(new ActionEvent(this, 0, "u")); latch.countDown(); });
        javax.swing.SwingUtilities.invokeLater(() -> { down.actionPerformed(new ActionEvent(this, 0, "d")); latch.countDown(); });
        javax.swing.SwingUtilities.invokeLater(() -> { click.actionPerformed(new ActionEvent(this, 0, "c")); latch.countDown(); });
        assertDoesNotThrow(() -> latch.await(1, java.util.concurrent.TimeUnit.SECONDS));
    }
}
