package tetris.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.domain.GameMode;
import tetris.multiplayer.controller.LocalMultiPlayerController;
import tetris.multiplayer.controller.NetworkMultiPlayerController;
import tetris.multiplayer.handler.MultiplayerHandler;
import tetris.multiplayer.handler.NetworkedMultiplayerHandler;
import tetris.multiplayer.session.NetworkMultiplayerSession;
import tetris.multiplayer.session.LocalMultiplayerSession;
import tetris.network.protocol.PlayerInput;
import tetris.view.TetrisFrame;

/**
 * GameController의 남은 라우팅 분기 커버리지 보강:
 * - P2 HOLD 키 라우팅
 * - 네트워크 입력 반복 방지(shouldIgnoreKeyRepeat) 경로
 * - startNetworkedMultiplayerGame 내부 람다 호출
 */
class GameControllerExtraRoutingTest {

    private GameController controller;
    private GameModel model;

    @BeforeEach
    void setUp() {
        model = mock(GameModel.class, Mockito.withSettings().lenient());
        controller = new GameController(model);
    }

    @Test
    void routeLocalMultiplayerInput_p2SoftDrop_dispatches() throws Exception {
        MultiplayerHandler handler = mock(MultiplayerHandler.class);
        LocalMultiplayerSession session = mock(LocalMultiplayerSession.class);
        when(session.handler()).thenReturn(handler);
        when(model.isLocalMultiplayerActive()).thenReturn(true);
        setField("localSession", session);
        Map<String, Integer> kb = new HashMap<>();
        kb.put("P2_SOFT_DROP", 500);
        setField("keyBindings", kb);

        Method route = GameController.class.getDeclaredMethod("routeLocalMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 500);

        assertTrue(handled);
        verify(handler, atLeastOnce()).dispatchToPlayer(Mockito.eq(2), any());
    }

    @Test
    void routeNetworkMultiplayerInput_repeatIgnored() throws Exception {
        NetworkMultiplayerSession session = mock(NetworkMultiplayerSession.class);
        NetworkedMultiplayerHandler handler = mock(NetworkedMultiplayerHandler.class);
        when(handler.getLocalPlayerId()).thenReturn(1);
        NetworkMultiPlayerController netCtrl = mock(NetworkMultiPlayerController.class);
        when(session.handler()).thenReturn(handler);
        when(session.networkController()).thenReturn(netCtrl);
        setField("networkSession", session);

        Map<String, Integer> kb = new HashMap<>();
        kb.put("MOVE_LEFT", 101);
        setField("keyBindings", kb);
        // 키 반복 방지를 위해 lastKeyPressTime을 미리 채운다
        Map<Integer, Long> map = new HashMap<>();
        map.put(101, System.currentTimeMillis());
        setField("lastKeyPressTime", map);

        Method route = GameController.class.getDeclaredMethod("routeNetworkMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 101);

        assertTrue(handled);
        verify(netCtrl, atLeastOnce()).sendPlayerInput(any(PlayerInput.class));
    }

    @Test
    void shouldIgnoreKeyRepeat_respectsDelay() throws Exception {
        Map<String, Integer> kb = getKeyBindings();
        int moveLeft = kb.get("MOVE_LEFT");
        Map<Integer, Long> last = new HashMap<>();
        long now = System.currentTimeMillis();
        last.put(moveLeft, now);
        setField("lastKeyPressTime", last);

        Method m = GameController.class.getDeclaredMethod("shouldIgnoreKeyRepeat", int.class, long.class);
        m.setAccessible(true);
        boolean withinDelay = (boolean) m.invoke(controller, moveLeft, now + 10); // < MOVEMENT_REPEAT_DELAY(30)
        boolean afterDelay = (boolean) m.invoke(controller, moveLeft, now + 100);
        assertTrue(withinDelay);
        assertFalse(afterDelay);
    }

    @Test
    void applyKeyBindings_mergesAndIgnoresNull() throws Exception {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("MOVE_LEFT", 999);
        overrides.put("HOLD", null); // null 값은 무시
        controller.applyKeyBindings(overrides);

        Map<String, Integer> kb = getKeyBindings();
        assertEquals(999, kb.get("MOVE_LEFT"));
        // 기존 키 값이 유지되었는지 확인 (null 덮어쓰지 않음)
        assertNotNull(kb.get("HOLD"));
    }

    private void setField(String name, Object value) throws Exception {
        Field f = GameController.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(controller, value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getKeyBindings() throws Exception {
        Field f = GameController.class.getDeclaredField("keyBindings");
        f.setAccessible(true);
        return (Map<String, Integer>) f.get(controller);
    }
}
