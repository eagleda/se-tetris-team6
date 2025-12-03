/**
 * 대상: TetrisFrame.new MainPanel() {...}의 onMultiPlayConfirmed/connectToServer 및 내부 람다들
 *
 * 목적:
 * - 온라인 멀티 분기 진입을 리플렉션으로 호출해 해당 메서드/람다의 미싱 라인을 최대한 커버한다.
 * - Headless 환경에서는 JDialog 생성이 HeadlessException을 던지므로 이를 기대 예외로 처리한다.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.GameModel;
import tetris.domain.score.ScoreRuleEngine;

class TetrisFrameMultiPlayTest {

    private GameModel model;
    private TetrisFrame frame;

    @BeforeEach
    void setUp() {
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
    void onMultiPlayConfirmed_localBranch_noThrow() throws Exception {
        // Headless 여부와 무관하게 로컬 분기는 UI를 띄우지 않으므로 직접 호출
        var mainPanelField = TetrisFrame.class.getDeclaredField("mainPanel");
        mainPanelField.setAccessible(true);
        Object mainPanel = mainPanelField.get(frame);
        Method m = mainPanel.getClass().getDeclaredMethod("onMultiPlayConfirmed", String.class, boolean.class, boolean.class);
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(mainPanel, "NORMAL", false, false));
    }

    @Test
    void connectToServer_onlineClientBranch_headlessThrowsOrReturns() throws Exception {
        var mainPanelField = TetrisFrame.class.getDeclaredField("mainPanel");
        mainPanelField.setAccessible(true);
        Object mainPanel = mainPanelField.get(frame);
        Method m = mainPanel.getClass().getDeclaredMethod("connectToServer", String.class);
        m.setAccessible(true);

        if (GraphicsEnvironment.isHeadless()) {
            assertThrows(Exception.class, () -> m.invoke(mainPanel, "127.0.0.1:1"));
        } else {
            // UI 환경에서는 호출 후 예외 없이 반환을 기대(실제 연결 실패 시 내부에서 처리)
            assertDoesNotThrow(() -> m.invoke(mainPanel, "127.0.0.1:1"));
        }
    }
}
