package tetris.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.concurrent.GameThread;
import tetris.concurrent.NetworkThread;
import tetris.domain.model.GameState;
import tetris.network.protocol.InputType;
import tetris.network.protocol.PlayerInput;

/*
 * 테스트 대상: tetris.network.NetworkManager
 *
 * 역할 요약:
 * - NetworkThread/Server/Client를 관리하고 GameThread와의 인터페이스를 제공하는 네트워크 관리 클래스.
 *
 * 테스트 전략:
 * - 초기 상태가 OFFLINE이고 isConnected/connectedPlayerCount가 예상대로 동작하는지 확인.
 * - networkThread를 주입해 isConnected()와 sendPlayerInput가 호출 흐름을 갖는지 검증.
 * - disconnect 호출 시 모드가 OFFLINE으로 복귀하는지 확인.
 */
@ExtendWith(MockitoExtension.class)
class NetworkManagerTest {

    @Mock GameThread gameThread;
    @Mock NetworkThread netThread;

    private NetworkManager manager;

    @BeforeEach
    void setUp() {
        manager = new NetworkManager(gameThread);
    }

    @Test
    void initialState_offlineAndNotConnected() {
        assertEquals(NetworkMode.OFFLINE, manager.getCurrentMode());
        assertFalse(manager.isConnected());
        assertEquals(1, manager.getConnectedPlayerCount());
    }

    @Test
    void isConnected_respectsInjectedNetworkThread() throws Exception {
        injectNetworkThread(netThread);
        when(netThread.isConnected()).thenReturn(true);

        assertTrue(manager.isConnected());
        assertEquals(2, manager.getConnectedPlayerCount());
    }

    @Test
    void sendPlayerInput_whenConnected_sendsMessageAndAppliesOptimistically() throws Exception {
        injectNetworkThread(netThread);
        when(netThread.isConnected()).thenReturn(true);
        doNothing().when(netThread).sendMessage(org.mockito.ArgumentMatchers.any());

        PlayerInput input = new PlayerInput(InputType.MOVE_LEFT);
        manager.sendPlayerInput(input);

        verify(netThread).sendMessage(org.mockito.ArgumentMatchers.any());
        // GameThread.applyImmediateInput는 mock이라 호출 여부만 검증
        verify(gameThread).applyImmediateInput(input);
    }

    @Test
    void disconnect_setsModeOffline() {
        manager.disconnect();
        assertEquals(NetworkMode.OFFLINE, manager.getCurrentMode());
    }

    private void injectNetworkThread(NetworkThread thread) throws Exception {
        Field f = NetworkManager.class.getDeclaredField("networkThread");
        f.setAccessible(true);
        f.set(manager, thread);
    }
}
