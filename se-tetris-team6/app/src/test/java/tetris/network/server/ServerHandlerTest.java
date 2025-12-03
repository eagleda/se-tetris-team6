package tetris.network.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.network.server.ServerHandler
 *
 * 역할 요약:
 * - 서버 측에서 각 클라이언트와의 소켓 연결을 관리하는 핸들러
 * - 백그라운드 스레드에서 클라이언트로부터 메시지를 수신
 * - 수신된 메시지를 GameServer로 전달하여 다른 클라이언트에 브로드캐스트
 * - 클라이언트 연결 끊김 감지 및 정리
 *
 * 테스트 전략:
 * - 메시지 수신 및 전달 로직을 중심으로 테스트
 * - 클라이언트 식별 및 메시지 라우팅 검증
 * - 예외 상황(연결 끊김, 잘못된 메시지) 처리 확인
 *
 * 사용 라이브러리:
 * - JUnit 5
 * - Mockito (Socket, GameServer Mock)
 *
 * 주요 테스트 시나리오:
 * - 클라이언트 메시지 수신 시 서버로 전달
 * - 연결 끊김 감지 시 서버에 통지
 * - 핸들러 종료 시 소켓 정리
 * - 여러 메시지 연속 처리
 */

@ExtendWith(MockitoExtension.class)
public class ServerHandlerTest {

    @Mock
    private Socket mockSocket;

    @Mock
    private GameServer mockGameServer;

    private ServerHandler serverHandler;
    private final String clientId = "client-001";

    @BeforeEach
    void setUp() {
        serverHandler = new ServerHandler(mockSocket, mockGameServer, clientId);
    }

    @Test
    void constructor_shouldInitializeWithSocketAndServer() {
        // then
        assertNotNull(serverHandler, "ServerHandler should be created");
        assertEquals(clientId, serverHandler.getClientId(), 
            "Client ID should match");
    }

    @Test
    void handleMessage_withPlayerInputMessage_shouldForwardToServer() {
        // given
        GameMessage message = new GameMessage(MessageType.PLAYER_INPUT);

        // when
        serverHandler.handleMessage(message);

        // then
        verify(mockGameServer).onClientMessage(clientId, message);
    }

    @Test
    void handleMessage_withAttackLinesMessage_shouldForwardToServer() {
        // given
        GameMessage message = new GameMessage(MessageType.ATTACK_LINES);

        // when
        serverHandler.handleMessage(message);

        // then
        verify(mockGameServer).onClientMessage(clientId, message);
    }

    @Test
    void handleMessage_withDisconnectMessage_shouldNotifyServer() {
        // given
        GameMessage message = new GameMessage(MessageType.DISCONNECT);

        // when
        serverHandler.handleMessage(message);

        // then
        verify(mockGameServer).onClientDisconnected(clientId);
    }

    @Test
    void sendMessage_shouldWriteToSocket() throws IOException {
        // given
        GameMessage message = new GameMessage(MessageType.GAME_START);

        // when
        boolean sent = serverHandler.sendMessage(message);

        // then
        assertTrue(sent, "Message should be sent successfully");
    }

    @Test
    void close_shouldCloseSocket() throws IOException {
        // when
        serverHandler.close();

        // then
        verify(mockSocket).close();
    }

    @Test
    void getClientId_shouldReturnAssignedId() {
        // when
        String id = serverHandler.getClientId();

        // then
        assertEquals(clientId, id, "Should return assigned client ID");
    }

    @Test
    void isConnected_shouldReturnTrueInitially() {
        // when
        boolean connected = serverHandler.isConnected();

        // then
        assertTrue(connected, "Should be connected after creation");
    }

    @Test
    void stop_shouldDisconnectHandler() {
        // when
        serverHandler.stop();
        boolean connected = serverHandler.isConnected();

        // then
        assertFalse(connected, "Should be disconnected after stop");
    }

    @Test
    void handleMessage_withNullMessage_shouldNotCrash() {
        // when & then
        assertDoesNotThrow(() -> {
            serverHandler.handleMessage(null);
        });
        verify(mockGameServer, never()).onClientMessage(anyString(), any());
    }
}
