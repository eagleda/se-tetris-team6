/**
 * 대상: tetris.view.TetrisFrame ($11 네트워크 상태 타이머)
 *
 * 목적:
 * - startNetworkStatusMonitoring/stopNetworkStatusMonitoring 호출 시 networkUpdateTimer가 생성/정리되고
 *   updateNetworkStatus 호출이 예외 없이 실행되는지 검증해 0% 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) startNetworkStatusMonitoring 후 networkUpdateTimer가 null이 아닌지 확인
 * 2) updateNetworkStatus가 예외 없이 실행되는지 확인
 * 3) stopNetworkStatusMonitoring 후 networkUpdateTimer가 null로 정리되는지 확인
 *
 * Mockito 사용 이유:
 * - GameModel/Controller 의존성을 간단히 스텁해 UI를 생성하기 위함.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.GameModel;
import tetris.domain.score.ScoreRuleEngine;

class TetrisFrameNetworkStatusTest {

    private TetrisFrame frame;
    private GameModel model;

    @BeforeEach
    void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        model = mock(GameModel.class, Mockito.withSettings().lenient());
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
    void startAndStopNetworkStatusMonitoring_managesTimer() throws Exception {
        // startMonitoring (private) 호출
        var start = TetrisFrame.class.getDeclaredMethod("startNetworkStatusMonitoring");
        start.setAccessible(true);
        start.invoke(frame);

        Field f = TetrisFrame.class.getDeclaredField("networkUpdateTimer");
        f.setAccessible(true);
        javax.swing.Timer timer = (javax.swing.Timer) f.get(frame);
        assertNotNull(timer);

        // updateNetworkStatus 호출 스모크
        var update = TetrisFrame.class.getDeclaredMethod("updateNetworkStatus");
        update.setAccessible(true);
        assertDoesNotThrow(() -> update.invoke(frame));

        // stopMonitoring (private) 호출
        var stop = TetrisFrame.class.getDeclaredMethod("stopNetworkStatusMonitoring");
        stop.setAccessible(true);
        stop.invoke(frame);
        assertNull(f.get(frame));
    }
}
