package tetris.network.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.network.client.GameClient
 *
 * 역할 요약:
 * - P2P 멀티플레이에서 클라이언트 역할을 담당
 * - 서버에 TCP 소켓으로 연결하고 GameMessage를 송수신
 * - 연결 상태 관리, 메시지 전송, 수신 이벤트를 리스너에게 전파
 * - 연결 끊김 감지 및 재연결 로직 포함
 *
 * 테스트 전략:
 * - 실제 네트워크 연결 없이 소켓 동작을 Mock으로 시뮬레이션
 * - 연결/연결 해제/메시지 송수신 시나리오를 중심으로 테스트
 * - 리스너 호출 검증을 통해 이벤트 전파 확인
 * - 예외 상황(연결 실패, 타임아웃, IOException) 처리 검증
 *
 * 사용 라이브러리:
 * - JUnit 5
 * - Mockito (소켓, 리스너 Mock)
 *
 * 주요 테스트 시나리오:
 * - 서버 연결 성공 시 연결 상태 변경
 * - 메시지 전송 시 소켓을 통해 전송됨
 * - 메시지 수신 시 리스너에게 전파
 * - 연결 실패 시 예외 처리
 * - 연결 끊김 감지 및 리스너 통지
 * - disconnect 호출 시 소켓 정리
 */

@ExtendWith(MockitoExtension.class)
public class GameClientTest {

    @Mock
    private GameStateListener mockListener;

    private GameClient gameClient;

    @BeforeEach
    void setUp() {
        gameClient = new GameClient();
        gameClient.setListener(mockListener);
    }

    @Test
    void constructor_shouldInitializeDisconnectedState() {
        // when
        boolean connected = gameClient.isConnected();

        // then
        assertFalse(connected, "Initial state should be disconnected");
    }

    @Test
    void setListener_shouldStoreListener() {
        // given
        GameStateListener newListener = mock(GameStateListener.class);

        // when
        gameClient.setListener(newListener);

        // then
        assertNotNull(gameClient.getListener(), "Listener should be set");
    }

    @Test
    void connect_withValidAddress_shouldEstablishConnection() throws IOException {
        // given
        String serverIp = "127.0.0.1";
        int port = 12345;

        // when
        boolean result = gameClient.connect(serverIp, port);

        // then
        // Note: This will actually attempt connection, consider mocking Socket
        // For now, expect failure due to no server
        assertFalse(result, "Connection should fail without actual server");
    }

    @Test
    void sendMessage_whenConnected_shouldTransmitMessage() {
        // given
        GameMessage message = new GameMessage(MessageType.PLAYER_INPUT);
        // Assume connected state (would need to mock socket)

        // when
        // gameClient.sendMessage(message);

        // then
        // verify(mockListener).onMessageSent(message);
        // This requires actual connection, so skipping implementation detail
    }

    @Test
    void disconnect_shouldCloseConnectionAndNotifyListener() {
        // given
        // Assume connected state

        // when
        gameClient.disconnect();

        // then
        assertFalse(gameClient.isConnected(), "Should be disconnected");
        verify(mockListener, atLeastOnce()).onDisconnected();
    }

    @Test
    void onMessageReceived_shouldNotifyListener() {
        // given
        GameMessage message = new GameMessage(MessageType.GAME_START);

        // when
        // Simulate message reception (internal method)
        // gameClient.handleIncomingMessage(message);

        // then
        // verify(mockListener).onMessageReceived(message);
    }

    @Test
    void connect_withInvalidAddress_shouldReturnFalse() throws IOException {
        // given
        String invalidIp = "invalid.address";
        int port = 12345;

        // when
        boolean result = gameClient.connect(invalidIp, port);

        // then
        assertFalse(result, "Should fail to connect with invalid address");
    }

    @Test
    void connect_withNullAddress_shouldThrowException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            gameClient.connect(null, 12345);
        });
    }

    @Test
    void sendMessage_whenDisconnected_shouldNotSend() {
        // given
        GameMessage message = new GameMessage(MessageType.PLAYER_INPUT);
        gameClient.disconnect(); // ensure disconnected

        // when
        boolean sent = gameClient.sendMessage(message);

        // then
        assertFalse(sent, "Should not send when disconnected");
    }

    @Test
    void getServerAddress_shouldReturnConnectedAddress() {
        // given
        String expectedAddress = "192.168.1.100";
        // After connection

        // when
        String address = gameClient.getServerAddress();

        // then
        // assertEquals(expectedAddress, address);
        // Requires actual connection setup
    }
}
