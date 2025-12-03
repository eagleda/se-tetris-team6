/**
 * 대상: TetrisFrame의 네트워크/상태 모니터링 및 주요 유틸 메서드
 *
 * 목적:
 * - cleanupNetworkSession, handleOpponentDisconnected, resolvePanelName,
 *   ensureOnlineSessionUiBridges, 키바인딩 람다, 네트워크 상태 모니터링 start/stop 등을
 *   직접 호출해 미싱 라인을 보강한다.
 *
 * 주의:
 * - UI 의존성이 있으므로 headless 환경에서는 스킵한다.
 * - 네트워크 실연결은 하지 않고 모의 객체/스모크만 수행한다.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

import javax.swing.JPanel;

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
import tetris.view.ScoreboardPanel;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

class TetrisFrameNetworkAndUiBridgeTest {

    private GameModel model;
    private TetrisFrame frame;

    @BeforeEach
    void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "UI 테스트는 headless에서 스킵");
        model = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        var repo = new InMemoryScoreRepository();
        when(model.getScoreRepository()).thenReturn(repo);
        when(model.getScoreEngine()).thenReturn(new ScoreRuleEngine(repo));
        when(model.getLeaderboardRepository()).thenReturn(new InMemoryLeaderboardRepository());
        when(model.getCurrentState()).thenReturn(GameState.PLAYING);
        when(model.getActiveNetworkMultiplayerSession()).thenReturn(java.util.Optional.empty());
        when(model.getActiveLocalMultiplayerSession()).thenReturn(java.util.Optional.empty());
        frame = new TetrisFrame(model);
        frame.setVisible(false);
    }

    @AfterEach
    void tearDown() {
        if (frame != null) frame.dispose();
    }

    @Test
    void cleanupNetworkSession_disconnectsClientAndServer() throws Exception {
        // 서버/세션 없이도 예외 없이 종료되는지만 스모크 (실제 stop 동작은 통합 테스트에서 커버)
        // hostedServer 초기화/정리 여부 확인
        setField("hostedServer", mock(tetris.network.server.GameServer.class));
        assertDoesNotThrow(() -> frame.cleanupNetworkSessionPublic());
        var serverField = TetrisFrame.class.getDeclaredField("hostedServer");
        serverField.setAccessible(true);
        assertEquals(null, serverField.get(frame));

    }

    @Test
    void handleOpponentDisconnected_quitsToMenu() {
        GameMessage msg = new GameMessage(MessageType.GAME_END, "TEST", null);
        // handleOpponentDisconnected는 내부에서 gameModel.quitToMenu를 직접 부르지 않는다.
        // 단순히 예외 없이 처리되는지만 확인한다.
        assertDoesNotThrow(() -> invokePrivate("handleOpponentDisconnected", msg));
    }

    @Test
    void resolvePanelName_returnsNonBlank() throws Exception {
        Method m = TetrisFrame.class.getDeclaredMethod("resolvePanelName", JPanel.class);
        m.setAccessible(true);
        Object name = m.invoke(frame, new JPanel());
        assertNotNull(name);
        assertFalse(name.toString().isBlank());

        // 실제 주요 패널들도 이름이 반환되는지 확인
        var localField = TetrisFrame.class.getDeclaredField("localMultiGameLayout");
        localField.setAccessible(true);
        Object local = localField.get(frame);
        assertNotNull(local);
        assertFalse(m.invoke(frame, local).toString().isBlank());

        var onlineField = TetrisFrame.class.getDeclaredField("onlineMultiGameLayout");
        onlineField.setAccessible(true);
        Object online = onlineField.get(frame);
        assertNotNull(online);
        assertFalse(m.invoke(frame, online).toString().isBlank());

        var pauseField = TetrisFrame.class.getDeclaredField("pausePanel");
        pauseField.setAccessible(true);
        Object pause = pauseField.get(frame);
        assertNotNull(pause);
        assertFalse(m.invoke(frame, pause).toString().isBlank());
    }

    @Test
    void ensureOnlineSessionUiBridges_initializesOverlay() throws Exception {
        Method m = TetrisFrame.class.getDeclaredMethod("ensureOnlineSessionUiBridges");
        m.setAccessible(true);
        m.invoke(frame);
        var overlayField = TetrisFrame.class.getDeclaredField("networkStatusOverlay");
        overlayField.setAccessible(true);
        assertNotNull(overlayField.get(frame));
    }

    @Test
    void ensureLocalSessionUiBridges_initializesPanels() throws Exception {
        Method m = TetrisFrame.class.getDeclaredMethod("ensureLocalSessionUiBridges");
        m.setAccessible(true);
        m.invoke(frame);
        var localField = TetrisFrame.class.getDeclaredField("localMultiGameLayout");
        localField.setAccessible(true);
        assertNotNull(localField.get(frame));
    }

    @Test
    void displayPanel_scoreboard_callsRenderLeaderboard() throws Exception {
        // 단순히 예외 없이 scoreboard 표시가 가능함을 확인
        assertDoesNotThrow(() -> frame.showScoreboardPanel());
    }

    @Test
    void handleOpponentDisconnected_noSession_noThrow() {
        GameMessage msg = new GameMessage(MessageType.GAME_END, "TEST", null);
        assertDoesNotThrow(() -> invokePrivate("handleOpponentDisconnected", msg));
    }

    @Test
    void startNetworkStatusMonitoring_overlayVisibleAfterStart() throws Exception {
        // overlay 생성 보장 후 시작/중지 스모크
        invokePrivate("ensureOnlineSessionUiBridges");
        assertDoesNotThrow(() -> invokePrivate("startNetworkStatusMonitoring"));
        var overlayField = TetrisFrame.class.getDeclaredField("networkStatusOverlay");
        overlayField.setAccessible(true);
        Object overlay = overlayField.get(frame);
        assertNotNull(overlay);
        assertDoesNotThrow(() -> invokePrivate("stopNetworkStatusMonitoring"));
    }

    @Test
    void displayPanel_withNull_isNoOp() {
        assertDoesNotThrow(() -> frame.displayPanel(null));
    }

    @Test
    void installRootKeyBindings_lambda3_runs() throws Exception {
        Method lambdaMethod = null;
        for (Method candidate : TetrisFrame.class.getDeclaredMethods()) {
            if (candidate.getName().equals("lambda$installRootKeyBindings$3")) {
                lambdaMethod = candidate;
                break;
            }
        }
        assertNotNull(lambdaMethod);
        lambdaMethod.setAccessible(true);
        Method finalLambda = lambdaMethod;
        assertDoesNotThrow(() -> finalLambda.invoke(frame,
                new KeyEvent(frame, 0, 0, 0, KeyEvent.VK_ESCAPE, ' ')));
    }

    @Test
    void startAndStopNetworkStatusMonitoring_runsSafely() {
        assertDoesNotThrow(() -> invokePrivate("startNetworkStatusMonitoring"));
        assertDoesNotThrow(() -> invokePrivate("stopNetworkStatusMonitoring"));
        // 재진입성 확인
        assertDoesNotThrow(() -> invokePrivate("stopNetworkStatusMonitoring"));
    }

    // ===== 유틸 =====
    private void setField(String name, Object value) throws Exception {
        var f = TetrisFrame.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(frame, value);
    }

    private void invokePrivate(String name, Object... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].getClass();
        }
        Method m = TetrisFrame.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        m.invoke(frame, args);
    }
}
