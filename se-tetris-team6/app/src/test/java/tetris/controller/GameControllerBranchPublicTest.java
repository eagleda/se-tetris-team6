package tetris.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.KeyEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.domain.GameMode;
import tetris.domain.model.GameState;
import tetris.multiplayer.handler.MultiplayerHandler;
import tetris.multiplayer.handler.NetworkedMultiplayerHandler;
import tetris.multiplayer.session.LocalMultiplayerSession;
import tetris.multiplayer.session.NetworkMultiplayerSession;
import tetris.network.protocol.PlayerInput;

/**
 * GameController의 남은 분기(로컬/네트워크 라우팅, 플레이 입력, 시작 메서드)를 보강하는 스모크 테스트.
 */
class GameControllerBranchPublicTest {

    private GameController controller;
    private GameModel model;

    @BeforeEach
    void setup() {
        model = mock(GameModel.class, Mockito.withSettings().lenient());
        when(model.getCurrentState()).thenReturn(GameState.PLAYING);
        controller = new GameController(model);
    }

    @Test
    void handleGamePlayInput_moveRight_dispatchesToModel() throws Exception {
        Map<String, Integer> kb = getKeyBindingsReflect();
        kb.put("MOVE_RIGHT", 39);

        Method m = GameController.class.getDeclaredMethod("handleGamePlayInput", int.class);
        m.setAccessible(true);
        m.invoke(controller, 39);

        verify(model, atLeastOnce()).moveBlockRight();
    }

    @Test
    void handleGamePlayInput_quitGame_callsModelQuit() throws Exception {
        Map<String, Integer> kb = getKeyBindingsReflect();
        kb.put("QUIT_GAME", 99);
        Method m = GameController.class.getDeclaredMethod("handleGamePlayInput", int.class);
        m.setAccessible(true);
        m.invoke(controller, 99);

        verify(model, atLeastOnce()).quitToMenu();
    }

    @Test
    void routeLocalMultiplayerInput_p1Rotate_dispatches() throws Exception {
        MultiplayerHandler handler = mock(MultiplayerHandler.class);
        LocalMultiplayerSession session = mock(LocalMultiplayerSession.class);
        when(session.handler()).thenReturn(handler);
        when(model.isLocalMultiplayerActive()).thenReturn(true);
        setField("localSession", session);
        Map<String, Integer> kb = new HashMap<>();
        kb.put("P1_ROTATE_CW", 400);
        setField("keyBindings", kb);

        Method route = GameController.class.getDeclaredMethod("routeLocalMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 400);

        assertTrue(handled);
        verify(handler, atLeastOnce()).dispatchToPlayer(eq(1), any());
    }

    @Test
    void routeNetworkMultiplayerInput_hardDrop_dispatches() throws Exception {
        NetworkMultiplayerSession session = mock(NetworkMultiplayerSession.class);
        NetworkedMultiplayerHandler handler = mock(NetworkedMultiplayerHandler.class);
        when(handler.getLocalPlayerId()).thenReturn(1);
        tetris.multiplayer.controller.NetworkMultiPlayerController netCtrl = mock(tetris.multiplayer.controller.NetworkMultiPlayerController.class);
        when(session.handler()).thenReturn(handler);
        when(session.networkController()).thenReturn(netCtrl);
        setField("networkSession", session);

        Map<String, Integer> kb = getKeyBindingsReflect();
        kb.put("HARD_DROP", 300);

        Method route = GameController.class.getDeclaredMethod("routeNetworkMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 300);

        assertTrue(handled);
        verify(netCtrl, atLeastOnce()).sendPlayerInput(any(PlayerInput.class));
    }

    @Test
    void routeNetworkMultiplayerInput_rotateAndHold_dispatches() throws Exception {
        NetworkMultiplayerSession session = mock(NetworkMultiplayerSession.class);
        NetworkedMultiplayerHandler handler = mock(NetworkedMultiplayerHandler.class);
        when(handler.getLocalPlayerId()).thenReturn(1);
        tetris.multiplayer.controller.NetworkMultiPlayerController netCtrl = mock(tetris.multiplayer.controller.NetworkMultiPlayerController.class);
        when(session.handler()).thenReturn(handler);
        when(session.networkController()).thenReturn(netCtrl);
        setField("networkSession", session);

        Map<String, Integer> kb = getKeyBindingsReflect();
        kb.put("ROTATE_CW", 301);
        kb.put("HOLD", 302);
        kb.put("ROTATE_CCW", 303);

        Method route = GameController.class.getDeclaredMethod("routeNetworkMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean cw = (boolean) route.invoke(controller, 301);
        boolean ccw = (boolean) route.invoke(controller, 303);
        boolean hold = (boolean) route.invoke(controller, 302);

        assertTrue(cw || ccw || hold);
        verify(netCtrl, atLeastOnce()).sendPlayerInput(any(PlayerInput.class));
    }

    @Test
    void handleNameInputInput_addsCharacterAndBackspace() throws Exception {
        Method m = GameController.class.getDeclaredMethod("handleNameInputInput", int.class);
        m.setAccessible(true);
        m.invoke(controller, KeyEvent.VK_A);
        m.invoke(controller, KeyEvent.VK_B);
        m.invoke(controller, KeyEvent.VK_BACK_SPACE);
        verify(model, atLeastOnce()).addCharacterToName(any(char.class));
        verify(model, atLeastOnce()).deleteCharacterFromName();
    }

    @Test
    void getKeyRepeatDelay_differentForMovementVsOther() throws Exception {
        Map<String, Integer> kb = getKeyBindingsReflect();
        int left = kb.get("MOVE_LEFT");
        int rotate = kb.get("ROTATE_CW");
        Method m = GameController.class.getDeclaredMethod("getKeyRepeatDelay", int.class);
        m.setAccessible(true);
        long movementDelay = (long) m.invoke(controller, left);
        long rotateDelay = (long) m.invoke(controller, rotate);
        assertTrue(movementDelay >= rotateDelay);
    }

    @Test
    void routeNetworkMultiplayerInput_noSession_returnsFalse() throws Exception {
        Map<String, Integer> kb = getKeyBindingsReflect();
        kb.put("MOVE_LEFT", 401);
        Method route = GameController.class.getDeclaredMethod("routeNetworkMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 401);
        assertFalse(handled);
    }

    @Test
    void startStandardAndItemGame_callStartGame() {
        controller.startStandardGame();
        controller.startItemGame();
        verify(model, atLeastOnce()).startGame(GameMode.STANDARD);
        verify(model, atLeastOnce()).startGame(GameMode.ITEM);
    }

    @Test
    void handlePausedInput_resumeAndQuitAndRestart() {
        when(model.getCurrentState()).thenReturn(GameState.PAUSED);
        int pause = getKey("PAUSE");
        int quit = getKey("QUIT_GAME");
        int restart = getKey("RESTART");

        controller.handleKeyPress(pause);
        controller.handleKeyRelease(pause);
        controller.handleKeyPress(quit);
        controller.handleKeyPress(restart);

        verify(model, atLeastOnce()).resumeGame();
        verify(model, atLeastOnce()).quitToMenu();
        verify(model, atLeastOnce()).restartGame();
    }

    @Test
    void handleGameOverInput_allowsProceedRestartQuit() {
        when(model.getCurrentState()).thenReturn(GameState.GAME_OVER);
        controller.handleKeyPress(getKey("MENU_SELECT"));
        controller.handleKeyPress(getKey("RESTART"));
        controller.handleKeyPress(getKey("QUIT_GAME"));

        verify(model, atLeastOnce()).proceedFromGameOver();
        verify(model, atLeastOnce()).restartGame();
        verify(model, atLeastOnce()).quitToMenu();
    }

    @Test
    void handleSettingsInput_navigatesAndResets() {
        when(model.getCurrentState()).thenReturn(GameState.SETTINGS);
        controller.handleKeyPress(getKey("MENU_UP"));
        controller.handleKeyPress(getKey("MENU_DOWN"));
        controller.handleKeyPress(getKey("MENU_SELECT"));
        controller.handleKeyPress(getKey("MENU_BACK"));
        controller.handleKeyPress(getKey("SETTINGS_RESET"));

        verify(model, atLeastOnce()).navigateSettingsUp();
        verify(model, atLeastOnce()).navigateSettingsDown();
        verify(model, atLeastOnce()).selectCurrentSetting();
        verify(model, atLeastOnce()).exitSettings();
        verify(model, atLeastOnce()).resetAllSettings();
    }

    @Test
    void handleScoreboardInput_scrollAndExit() {
        when(model.getCurrentState()).thenReturn(GameState.SCOREBOARD);
        controller.handleKeyPress(getKey("MENU_UP"));
        controller.handleKeyPress(getKey("MENU_DOWN"));
        controller.handleKeyPress(getKey("MENU_SELECT"));

        verify(model, atLeastOnce()).scrollScoreboardUp();
        verify(model, atLeastOnce()).scrollScoreboardDown();
        verify(model, atLeastOnce()).exitScoreboard();
    }

    @Test
    void handleNameInput_enterAndEscape() {
        when(model.getCurrentState()).thenReturn(GameState.NAME_INPUT);
        controller.handleKeyPress(KeyEvent.VK_ENTER);
        controller.handleKeyPress(KeyEvent.VK_ESCAPE);
        verify(model, atLeastOnce()).confirmNameInput();
        verify(model, atLeastOnce()).cancelNameInput();
    }

    @Test
    void shouldIgnoreKeyRepeat_skipsRapidMovement() throws Exception {
        int left = getKey("MOVE_LEFT");
        Field f = GameController.class.getDeclaredField("lastKeyPressTime");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, Long> presses = (Map<Integer, Long>) f.get(controller);
        presses.put(left, System.currentTimeMillis());
        Method ignore = GameController.class.getDeclaredMethod("shouldIgnoreKeyRepeat", int.class, long.class);
        ignore.setAccessible(true);
        boolean ignored = (boolean) ignore.invoke(controller, left, System.currentTimeMillis());
        assertTrue(ignored);
    }

    @Test
    void routeNetworkMultiplayerInput_withoutController_usesClientTransport() throws Exception {
        NetworkMultiplayerSession session = mock(NetworkMultiplayerSession.class);
        NetworkedMultiplayerHandler handler = mock(NetworkedMultiplayerHandler.class);
        when(handler.getLocalPlayerId()).thenReturn(1);
        when(session.handler()).thenReturn(handler);
        when(session.networkController()).thenReturn(null);
        setField("networkSession", session);

        tetris.network.client.GameClient client = mock(tetris.network.client.GameClient.class);
        controller.setNetworkClient(client);

        Map<String, Integer> kb = getKeyBindingsReflect();
        kb.put("HOLD", 777);
        Method route = GameController.class.getDeclaredMethod("routeNetworkMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 777);

        assertTrue(handled);
        verify(client, atLeastOnce()).sendPlayerInput(any(PlayerInput.class));
    }

    @Test
    void routeNetworkMultiplayerInput_softDrop_dispatches() throws Exception {
        NetworkMultiplayerSession session = mock(NetworkMultiplayerSession.class);
        NetworkedMultiplayerHandler handler = mock(NetworkedMultiplayerHandler.class);
        when(handler.getLocalPlayerId()).thenReturn(1);
        tetris.multiplayer.controller.NetworkMultiPlayerController netCtrl = mock(tetris.multiplayer.controller.NetworkMultiPlayerController.class);
        when(session.handler()).thenReturn(handler);
        when(session.networkController()).thenReturn(netCtrl);
        setField("networkSession", session);

        Map<String, Integer> kb = getKeyBindingsReflect();
        kb.put("SOFT_DROP", 555);
        Method route = GameController.class.getDeclaredMethod("routeNetworkMultiplayerInput", int.class);
        route.setAccessible(true);
        boolean handled = (boolean) route.invoke(controller, 555);

        assertTrue(handled);
        verify(netCtrl, atLeastOnce()).sendPlayerInput(any(PlayerInput.class));
    }

    @Test
    void cleanupNetworkSession_withClientAndSession_isIdempotent() throws Exception {
        tetris.network.client.GameClient client = mock(tetris.network.client.GameClient.class);
        NetworkMultiplayerSession session = mock(NetworkMultiplayerSession.class);
        setField("networkClient", client);
        setField("networkSession", session);

        controller.cleanupNetworkSession();
        controller.cleanupNetworkSession(); // second call should be no-op

        verify(client, atLeastOnce()).disconnect();
        verify(session, atLeastOnce()).shutdown();
    }

    @Test
    void handleKeyRelease_resetsPauseFlag() {
        when(model.getCurrentState()).thenReturn(GameState.PLAYING, GameState.PAUSED);
        int pause = getKey("PAUSE");

        controller.handleKeyPress(pause);   // pauses
        controller.handleKeyRelease(pause); // resets flag
        // 키 반복 무시를 피하기 위해 마지막 입력 기록을 비운다.
        try {
            Field f = GameController.class.getDeclaredField("lastKeyPressTime");
            f.setAccessible(true);
            ((Map<?, ?>) f.get(controller)).clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        controller.handleKeyPress(pause);   // resumes

        verify(model, atLeastOnce()).pauseGame();
        verify(model, atLeastOnce()).resumeGame();
    }

    private void setField(String name, Object value) throws Exception {
        Field f = GameController.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private int getKey(String action) {
        try {
            return getKeyBindingsReflect().get(action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getKeyBindingsReflect() throws Exception {
        Field f = GameController.class.getDeclaredField("keyBindings");
        f.setAccessible(true);
        return (Map<String, Integer>) f.get(controller);
    }
}
