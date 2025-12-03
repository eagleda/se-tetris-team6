/**
 * 대상: tetris.controller.GameController 주요 라우팅/멀티플레이 분기
 *
 * 목적:
 * - 네트워크 입력 라우팅, 로컬 멀티 라우팅, 게임플레이 입력(PAUSE/QUIT) 처리,
 *   네트워크 멀티 시작 및 GAME_END 콜백 분기를 직접 호출해 미싱 라인을 줄인다.
 *
 * 주요 시나리오:
 * 1) routeNetworkMultiplayerInput: 서버(P1) 역할에서 dispatch + sendPlayerInput 호출 확인
 * 2) handleGamePlayInput: PAUSE 키, QUIT_GAME 키 처리 확인
 * 3) routeLocalMultiplayerInput: 로컬 멀티 핸들러 dispatch 확인
 * 4) startNetworkedMultiplayerGame(mode, localIsP1): 세션 생성 후 enableNetworkMultiplayer/startGame 실행
 * 5) lambda$startNetworkedMultiplayerGame$0: 로컬이 P1일 때 GAME_END 클라이언트 메시지 전송 콜백 실행
 * 6) lambda$startNetworkedMultiplayerGame$1: 로컬이 P2일 때 GAME_END 서버 메시지 전송 콜백 실행
 */
package tetris.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.GameDifficulty;
import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.multiplayer.handler.MultiplayerHandler;
import tetris.multiplayer.handler.NetworkedMultiplayerHandler;
import tetris.multiplayer.session.NetworkMultiplayerSession;
import tetris.network.client.GameClient;
import tetris.network.server.GameServer;
import tetris.network.protocol.PlayerInput;

class GameControllerRoutingCoverageTest {

    private GameModel model;
    private GameController controller;

    @BeforeEach
    void setUp() {
        model = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        when(model.getCurrentState()).thenReturn(GameState.PLAYING);
        when(model.getBlockGenerator()).thenReturn(new DummyBlockGenerator());
        controller = new GameController(model);
    }

    @Test
    void routeNetworkMultiplayerInput_serverDispatchesAndSends() throws Exception {
        NetworkMultiplayerSession session = mock(NetworkMultiplayerSession.class);
        NetworkedMultiplayerHandler handler = mock(NetworkedMultiplayerHandler.class);
        when(handler.getLocalPlayerId()).thenReturn(1); // 서버 역할
        var netController = mock(tetris.multiplayer.controller.NetworkMultiPlayerController.class);

        when(session.handler()).thenReturn(handler);
        when(session.networkController()).thenReturn(netController);

        setField(controller, "networkSession", session);
        Map<String, Integer> map = new HashMap<>();
        map.put("MOVE_LEFT", 100);
        setField(controller, "keyBindings", map);

        Method route = GameController.class.getDeclaredMethod("routeNetworkMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 100);
        assertTrue(handled);

        verify(handler, times(1)).dispatchToPlayer(eq(1), any());
        verify(netController, times(1)).sendPlayerInput(any(PlayerInput.class));
    }

    @Test
    void routeNetworkMultiplayerInput_otherKeys_areHandled() throws Exception {
        NetworkMultiplayerSession session = mock(NetworkMultiplayerSession.class);
        NetworkedMultiplayerHandler handler = mock(NetworkedMultiplayerHandler.class);
        when(handler.getLocalPlayerId()).thenReturn(1); // 서버
        var netController = mock(tetris.multiplayer.controller.NetworkMultiPlayerController.class);
        when(session.handler()).thenReturn(handler);
        when(session.networkController()).thenReturn(netController);
        setField(controller, "networkSession", session);

        Map<String, Integer> map = new HashMap<>();
        map.put("MOVE_LEFT", 101);
        map.put("MOVE_RIGHT", 102);
        map.put("SOFT_DROP", 103);
        map.put("ROTATE_CW", 104);
        map.put("ROTATE_CCW", 201);
        map.put("HARD_DROP", 204);
        map.put("HOLD", 202);
        setField(controller, "keyBindings", map);

        Method route = GameController.class.getDeclaredMethod("routeNetworkMultiplayerInput", int.class);
        route.setAccessible(true);

        int handledCount = 0;
        if ((boolean) route.invoke(controller, 101)) handledCount++;
        if ((boolean) route.invoke(controller, 102)) handledCount++;
        if ((boolean) route.invoke(controller, 201)) handledCount++;
        if ((boolean) route.invoke(controller, 202)) handledCount++;
        if ((boolean) route.invoke(controller, 204)) handledCount++;

        assertTrue(handledCount >= 1, "At least one network input branch should be handled");
        verify(netController, atLeast(1)).sendPlayerInput(any(PlayerInput.class));
    }

    @Test
    void handleGamePlayInput_pauseThenQuit() throws Exception {
        Map<String, Integer> map = new HashMap<>();
        map.put("PAUSE", 10);
        map.put("QUIT_GAME", 11);
        setField(controller, "keyBindings", map);

        Method handle = GameController.class.getDeclaredMethod("handleGamePlayInput", int.class);
        handle.setAccessible(true);

        handle.invoke(controller, 10); // pause
        verify(model, times(1)).pauseGame();

        handle.invoke(controller, 11); // quit
        verify(model, times(1)).quitToMenu();
    }

    @Test
    void routeLocalMultiplayerInput_dispatchesToHandler() throws Exception {
        var localSession = mock(tetris.multiplayer.session.LocalMultiplayerSession.class);
        MultiplayerHandler handler = mock(MultiplayerHandler.class);
        when(localSession.handler()).thenReturn(handler);
        setField(controller, "localSession", localSession);
        when(model.isLocalMultiplayerActive()).thenReturn(true);

        Map<String, Integer> map = new HashMap<>();
        map.put("P1_MOVE_LEFT", 101);
        setField(controller, "keyBindings", map);

        Method route = GameController.class.getDeclaredMethod("routeLocalMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 101);
        assertTrue(handled);
        verify(handler, times(1)).dispatchToPlayer(eq(1), any());
    }

    @Test
    void startNetworkedMultiplayerGame_createsSessionAndStarts() {
        GameClient client = mock(GameClient.class);
        setField(controller, "networkClient", client);

        NetworkMultiplayerSession session = controller.startNetworkedMultiplayerGame(GameMode.STANDARD, true);
        assertNotNull(session);
        verify(model, atLeastOnce()).enableNetworkMultiplayer(session);
        verify(model, atLeastOnce()).startGame(GameMode.STANDARD);
    }

    @Test
    void startNetworkedMultiplayerGame_sendGameEnd_clientPath() throws Exception {
        GameClient client = mock(GameClient.class);
        setField(controller, "networkClient", client);

        NetworkMultiplayerSession session = controller.startNetworkedMultiplayerGame(GameMode.STANDARD, true);
        NetworkedMultiplayerHandler handler = (NetworkedMultiplayerHandler) session.handler();
        Field f = handler.getClass().getDeclaredField("sendGameEndCallback");
        f.setAccessible(true);
        Runnable cb = (Runnable) f.get(handler);
        cb.run();

        verify(client, times(1)).sendMessage(any());
    }

    @Test
    void startNetworkedMultiplayerGame_sendGameEnd_serverPath() throws Exception {
        GameServer server = mock(GameServer.class);
        setField(controller, "networkServer", server);

        NetworkMultiplayerSession session = controller.startNetworkedMultiplayerGame(GameMode.STANDARD, false);
        NetworkedMultiplayerHandler handler = (NetworkedMultiplayerHandler) session.handler();
        Field f = handler.getClass().getDeclaredField("sendGameEndCallback");
        f.setAccessible(true);
        Runnable cb = (Runnable) f.get(handler);
        cb.run();

        verify(server, times(1)).sendHostMessage(any());
    }

    // ==== 유틸/더미 ====
    private void setField(Object target, String name, Object value) {
        try {
            Field f = GameController.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            fail(e);
        }
    }

    private static class DummyBlockGenerator implements tetris.domain.BlockGenerator {
        @Override public tetris.domain.BlockKind nextBlock() { return tetris.domain.BlockKind.I; }
        @Override public void setDifficulty(GameDifficulty difficulty) {}
    }
}
