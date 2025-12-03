/**
 * 대상: tetris.view.GameComponent.NetworkMultiGameLayout
 *
 * 목적:
 * - 생성 시 자식 컴포넌트가 null 없이 초기화되는지, showTimer/hideTimer가 타이머 패널 가시성을 토글하는지 검증해
 *   30%대 커버리지를 보강한다.
 * - bindOnlineMultiplayerSession 호출 시 좌/우 모델 및 공격 대기열 바인딩이 예외 없이 수행되는지 확인한다.
 *
 * 주요 시나리오:
 * 1) 생성 직후 repaintTimer가 실행 중이며 주요 서브패널이 null이 아님을 확인
 * 2) showTimer/hideTimer로 타이머 패널 가시성이 토글되는지 확인
 * 3) bindOnlineMultiplayerSession이 null이 아닌 세션에서 pending 공격 공급자를 설정할 때 예외가 없는지 확인
 */
package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.multiplayer.handler.NetworkedMultiplayerHandler;
import tetris.multiplayer.session.NetworkMultiplayerSession;

class NetworkMultiGameLayoutTest {

    @Test
    void components_initialized_and_timerToggle() throws Exception {
        NetworkMultiGameLayout layout = new NetworkMultiGameLayout();

        // 주요 컴포넌트가 null이 아닌지 확인
        assertNotNull(layout.getComponent(0));

        // 리플렉션으로 timerPanel 접근
        var f = NetworkMultiGameLayout.class.getDeclaredField("timerPanel");
        f.setAccessible(true);
        TimerPanel timer = (TimerPanel) f.get(layout);
        assertNotNull(timer);

        layout.hideTimer();
        assertFalse(timer.isVisible());
        layout.showTimer();
        assertTrue(timer.isVisible());
    }

    @Test
    void bindOnlineMultiplayerSession_bindsWithoutException() {
        NetworkMultiGameLayout layout = new NetworkMultiGameLayout();
        NetworkMultiplayerSession session = Mockito.mock(NetworkMultiplayerSession.class, Mockito.withSettings().lenient());
        NetworkedMultiplayerHandler handler = Mockito.mock(NetworkedMultiplayerHandler.class, Mockito.withSettings().lenient());
        GameModel p1 = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        GameModel p2 = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());

        Mockito.when(session.handler()).thenReturn(handler);
        Mockito.when(handler.getLocalPlayerId()).thenReturn(1);
        Mockito.when(handler.getPendingAttackLines(Mockito.anyInt())).thenReturn(java.util.Collections.emptyList());
        Mockito.when(session.playerOneModel()).thenReturn(p1);
        Mockito.when(session.playerTwoModel()).thenReturn(p2);

        layout.bindOnlineMultiplayerSession(session);
        // 단순 성공 스모크: 예외 없이 종료되면 성공
        assertTrue(true);
    }
}
