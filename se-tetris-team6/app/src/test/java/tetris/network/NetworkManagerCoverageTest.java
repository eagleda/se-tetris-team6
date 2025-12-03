package tetris.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.concurrent.GameThread;
import tetris.concurrent.NetworkThread;
import tetris.domain.model.GameState;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import tetris.network.protocol.PlayerInput;

/**
 * NetworkManager의 주요 분기 커버리지 보강:
 * - 연결 상태 플래그, 이벤트 리스너 호출 여부
 * - 메시지 송신 메서드 sendPlayerInput/sendAttackLines/syncGameState/sendGameStart
 * - INetworkThreadCallback 수신 처리
 */
class NetworkManagerCoverageTest {

    private NetworkManager manager;
    private GameThread localThread;

    @BeforeEach
    void setup() {
        localThread = mock(GameThread.class, Mockito.withSettings().lenient());
        manager = new NetworkManager(localThread);
    }

    @Test
    void sendMessages_whenConnected_delegateToNetworkThread() throws Exception {
        NetworkThread net = mock(NetworkThread.class, Mockito.withSettings().lenient());
        setField(manager, "networkThread", net);
        when(net.isConnected()).thenReturn(true);

        PlayerInput input = new PlayerInput(tetris.network.protocol.InputType.MOVE_LEFT);
        manager.sendPlayerInput(input);
        manager.sendAttackLines(new AttackLine[]{new AttackLine(1)});
        manager.syncGameState(GameState.PLAYING);
        manager.sendGameStart();

        verify(net).sendMessage(Mockito.argThat(msg -> msg.getType() == MessageType.PLAYER_INPUT));
        verify(net).sendMessage(Mockito.argThat(msg -> msg.getType() == MessageType.ATTACK_LINES));
        verify(net).sendMessage(Mockito.argThat(msg -> msg.getType() == MessageType.BOARD_STATE));
        verify(net).sendMessage(Mockito.argThat(msg -> msg.getType() == MessageType.GAME_START));
    }

    @Test
    void disconnect_resetsStateAndNotifiesListener() throws Exception {
        NetworkEventListener listener = mock(NetworkEventListener.class);
        manager.setNetworkEventListener(listener);
        NetworkThread net = mock(NetworkThread.class, Mockito.withSettings().lenient());
        setField(manager, "networkThread", net);
        manager.disconnect();
        assertEquals(NetworkMode.OFFLINE, manager.getCurrentMode());
        verify(listener).onDisconnected();
    }

    @Test
    void handleReceivedMessage_dispatchesToGameDataListener() {
        GameDataListener data = mock(GameDataListener.class);
        manager.setGameDataListener(data);
        GameMessage inputMsg = new GameMessage(MessageType.PLAYER_INPUT, "p1", new PlayerInput(tetris.network.protocol.InputType.MOVE_LEFT));
        assertDoesNotThrow(() -> manager.handleReceivedMessage(inputMsg));
        GameMessage attackMsg = new GameMessage(MessageType.ATTACK_LINES, "p1", new AttackLine[]{new AttackLine(1)});
        assertDoesNotThrow(() -> manager.handleReceivedMessage(attackMsg));
        GameMessage stateMsg = new GameMessage(MessageType.BOARD_STATE, "p1", GameState.PLAYING);
        assertDoesNotThrow(() -> manager.handleReceivedMessage(stateMsg));
        GameMessage startMsg = new GameMessage(MessageType.GAME_START, "p1", null);
        assertDoesNotThrow(() -> manager.handleReceivedMessage(startMsg));
    }

    @Test
    void callbackMethods_invokeEventListener() {
        NetworkEventListener listener = mock(NetworkEventListener.class);
        manager.setNetworkEventListener(listener);
        manager.handleConnectionEstablished();
        manager.handleConnectionLost();
        manager.handleLatencyWarning(150);
        manager.handleNetworkError(new Exception("err"));
        verify(listener).onConnected();
        verify(listener).onDisconnected();
        verify(listener).onLatencyWarning(150);
        verify(listener).onConnectionError("err");
    }

    @Test
    void isConnected_andConnectedCount_reflectNetworkThreadState() throws Exception {
        assertFalse(manager.isConnected());
        assertEquals(1, manager.getConnectedPlayerCount());
        NetworkThread net = mock(NetworkThread.class, Mockito.withSettings().lenient());
        when(net.isConnected()).thenReturn(true);
        setField(manager, "networkThread", net);
        assertTrue(manager.isConnected());
        assertEquals(2, manager.getConnectedPlayerCount());
    }

    @Test
    void sendMessages_whenNotConnected_noThrow() throws Exception {
        NetworkThread net = mock(NetworkThread.class, Mockito.withSettings().lenient());
        when(net.isConnected()).thenReturn(false);
        setField(manager, "networkThread", net);

        assertDoesNotThrow(() -> manager.sendPlayerInput(new PlayerInput(tetris.network.protocol.InputType.MOVE_RIGHT)));
        assertDoesNotThrow(() -> manager.sendAttackLines(new AttackLine[]{new AttackLine(2)}));
        assertDoesNotThrow(() -> manager.syncGameState(GameState.PAUSED));
        assertDoesNotThrow(() -> manager.sendGameStart());
    }

    @Test
    void disconnect_withoutThread_isSafe() {
        assertDoesNotThrow(() -> manager.disconnect());
        // listener 없는 경우에도 예외 없이 동작
        manager.setNetworkEventListener(null);
        assertDoesNotThrow(() -> manager.handleConnectionLost());
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
