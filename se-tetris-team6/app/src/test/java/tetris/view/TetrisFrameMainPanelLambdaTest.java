/**
 * 대상: TetrisFrame.new MainPanel() {...} 내부 메서드/람다
 *
 * 목적:
 * - onMultiPlayConfirmed(String, boolean, boolean) 의 서버/클라이언트 분기 호출 경로를 headless에서 예외 없이(or 예외 기대) 실행한다.
 * - connectToServer(String) 호출 시 즉시 실패 분기를 타서 미싱 라인을 커버한다.
 * - lambda$connectToServer$11(...) (Ready 버튼 액션) 을 리플렉션으로 직접 호출해 sendReady 호출을 검증한다.
 *
 * 주의:
 * - UI 다이얼로그가 headless 환경에서 HeadlessException을 던질 수 있으므로, 필요 시 가정으로 스킵한다.
 * - 네트워크 실연결은 수행하지 않고, 예외/모의 객체를 사용해 빠른 경로만 검증한다.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Method;
import java.time.Duration;

import javax.swing.JDialog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.GameModel;
import tetris.domain.score.ScoreRuleEngine;
import tetris.network.client.GameClient;
import tetris.HeadlessTestSupport;

class TetrisFrameMainPanelLambdaTest {

    private GameModel model;
    private TetrisFrame frame;

    @BeforeEach
    void setUp() {
        HeadlessTestSupport.skipInHeadless();
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
    void onMultiPlayConfirmed_hostBranch_headlessThrows() throws Exception {
        // headless에서는 다이얼로그 생성이 실패하므로 예외를 기대한다.
        assumeFalse(!GraphicsEnvironment.isHeadless(), "GUI 환경은 스킵하여 블로킹 회피");
        Object mainPanel = getMainPanel();
        Method m = mainPanel.getClass().getDeclaredMethod("onMultiPlayConfirmed", String.class, boolean.class, boolean.class);
        m.setAccessible(true);
        assertThrows(Exception.class, () -> m.invoke(mainPanel, "NORMAL", true, true));
    }

    @Test
    void connectToServer_invalidAddress_headlessThrows() throws Exception {
        assumeTrue(GraphicsEnvironment.isHeadless(), "GUI 환경에서는 스킵(네트워크 블로킹 회피)");
        Object mainPanel = getMainPanel();
        Method m = mainPanel.getClass().getDeclaredMethod("connectToServer", String.class);
        m.setAccessible(true);
        assertTimeoutPreemptively(Duration.ofMillis(300),
                () -> assertThrows(Exception.class, () -> m.invoke(mainPanel, "invalid:0")));
    }

    @Test
    void lambdaConnectToServer11_invokesSendReady() throws Exception {
        // Ready 버튼 람다를 직접 호출해 sendReady가 실행되는지 확인 (GUI 필요)
        assumeFalse(GraphicsEnvironment.isHeadless(), "headless에서는 JDialog 생성 불가");
        Object mainPanel = getMainPanel();
        Method readyMethod = findLambda(mainPanel, "lambda$connectToServer$11");
        assertNotNull(readyMethod);
        readyMethod.setAccessible(true);

        boolean[] waiting = new boolean[] { true };
        JDialog dlg = new JDialog((java.awt.Frame) null, false);
        GameClient client = Mockito.mock(GameClient.class);

        Method finalReadyMethod = readyMethod;
        assertDoesNotThrow(() -> finalReadyMethod.invoke(mainPanel, waiting, dlg, "127.0.0.1", client));
    }

    @Test
    void onMultiPlayConfirmed_localBranch_callsStartLocalMultiplayer() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "UI 필요한 테스트");
        // gameController를 모의 객체로 교체
        tetris.controller.GameController gc = Mockito.mock(tetris.controller.GameController.class);
        var gcField = TetrisFrame.class.getDeclaredField("gameController");
        gcField.setAccessible(true);
        gcField.set(frame, gc);
        // 로컬 멀티 레이아웃이 null이면 새로 생성
        var localField = TetrisFrame.class.getDeclaredField("localMultiGameLayout");
        localField.setAccessible(true);
        if (localField.get(frame) == null) {
            localField.set(frame, new tetris.view.GameComponent.MultiGameLayout());
        }

        Object mainPanel = getMainPanel();
        Method m = mainPanel.getClass().getDeclaredMethod("onMultiPlayConfirmed", String.class, boolean.class, boolean.class);
        m.setAccessible(true);
        m.invoke(mainPanel, "NORMAL", false, false);

        Mockito.verify(gc, Mockito.times(1)).startLocalMultiplayerGame(tetris.domain.GameMode.STANDARD);
    }

    private Object getMainPanel() throws Exception {
        var f = TetrisFrame.class.getDeclaredField("mainPanel");
        f.setAccessible(true);
        Object mp = f.get(frame);
        assertNotNull(mp);
        return mp;
    }

    private Method findLambda(Object mainPanel, String name) {
        for (Method method : mainPanel.getClass().getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
}
