/**
 * 대상: tetris.view.TetrisFrame (applyScreenSize)
 *
 * 목적:
 * - applyScreenSize 호출 시 layeredPane과 자식 패널의 크기가 갱신되는지를 스모크해 화면 해상도 관련 미싱 라인을 보강한다.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.GameModel;
import tetris.domain.score.ScoreRuleEngine;
import tetris.domain.setting.Setting;

class TetrisFrameScreenSizeTest {

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
    void applyScreenSize_updatesLayeredPaneAndChildren() throws Exception {
        frame.applyScreenSize(Setting.ScreenSize.SMALL);

        Dimension expected = Setting.ScreenSize.SMALL.getDimension();
        var lpField = TetrisFrame.class.getDeclaredField("layeredPane");
        lpField.setAccessible(true);
        JLayeredPane pane = (JLayeredPane) lpField.get(frame);
        assertEquals(expected, pane.getPreferredSize());

        for (var comp : pane.getComponents()) {
            if (comp instanceof JPanel panel) {
                assertEquals(expected, panel.getPreferredSize());
            }
        }
    }
}
