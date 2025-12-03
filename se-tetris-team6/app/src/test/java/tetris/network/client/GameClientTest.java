package tetris.network.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import tetris.network.protocol.PlayerInput;

/*
 * 테스트 대상: tetris.network.client.GameClient
 *
 * 역할 요약:
 * - 서버와의 연결/핸들러를 관리하고 플레이어 입력/공격/스냅샷을 전송하는 클라이언트.
 *
 * 테스트 전략:
 * - 초기 상태 플래그가 false인지 확인.
 * - clientHandler를 주입해 sendMessage/sendReady/sendPlayerInput이 위임되는지 검증.
 * - disconnect 호출 시 isConnected 플래그가 내려가는지 확인.
 */
@ExtendWith(MockitoExtension.class)
class GameClientTest {

    @Mock ClientHandler handler;

    private GameClient client;

    @BeforeEach
    void setUp() {
        client = new GameClient();
        // 연결 플래그와 핸들러를 반영구성
        injectHandlerAndConnected(handler, true);
    }

    @Test
    void initialFlagsAreFalse() {
        GameClient fresh = new GameClient();
        assertFalse(fresh.isConnected());
        assertFalse(fresh.isStartReceived());
    }

    @Test
    void sendMessage_delegatesToHandler() {
        GameMessage msg = new GameMessage(MessageType.PING, "client", null);
        doNothing().when(handler).sendMessage(msg);

        client.sendMessage(msg);

        verify(handler).sendMessage(msg);
    }

    @Test
    void sendPlayerInput_delegatesToHandler() {
        PlayerInput input = new PlayerInput(tetris.network.protocol.InputType.MOVE_LEFT);
        doNothing().when(handler).sendMessage(org.mockito.ArgumentMatchers.any());

        client.sendPlayerInput(input);

        verify(handler).sendMessage(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendReady_sendsReadyMessage() {
        doNothing().when(handler).sendMessage(org.mockito.ArgumentMatchers.any());

        client.sendReady();

        verify(handler).sendMessage(org.mockito.ArgumentMatchers.any(GameMessage.class));
    }

    @Test
    void disconnect_clearsConnectedFlag() {
        doNothing().when(handler).sendMessage(org.mockito.ArgumentMatchers.any());
        client.disconnect();
        assertFalse(client.isConnected());
    }

    private void injectHandlerAndConnected(ClientHandler clientHandler, boolean connected) {
        try {
            Field handlerField = GameClient.class.getDeclaredField("clientHandler");
            handlerField.setAccessible(true);
            handlerField.set(client, clientHandler);

            Field connectedField = GameClient.class.getDeclaredField("isConnected");
            connectedField.setAccessible(true);
            connectedField.setBoolean(client, connected);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
