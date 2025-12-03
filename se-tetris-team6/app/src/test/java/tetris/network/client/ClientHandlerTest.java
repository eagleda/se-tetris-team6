package tetris.network.client;

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
 * 테스트 대상: tetris.network.client.ClientHandler
 *
 * 역할 요약:
 * - 클라이언트 측에서 서버와의 소켓 연결을 관리하는 핸들러
 * - 백그라운드 스레드에서 서버로부터 메시지를 수신하고 처리
 * - 수신된 메시지를 GameClient로 전달하여 게임 로직에 반영
 * - 연결 끊김 및 IO 예외를 감지하고 적절히 처리
 *
 * 테스트 전략:
 * - 메시지 수신 및 파싱 로직을 중심으로 테스트
 * - 스레드 동작은 직접 테스트하지 않고, 메시지 처리 로직만 검증
 * - 예외 상황(잘못된 메시지, 연결 끊김) 처리 확인
 *
 * 사용 라이브러리:
 * - JUnit 5
 * - Mockito (Socket, InputStream Mock)
 *
 * 주요 테스트 시나리오:
 * - 유효한 메시지 수신 시 올바르게 파싱
 * - 파싱된 메시지를 클라이언트에 전달
 * - IOException 발생 시 핸들러 종료
 * - 잘못된 형식의 메시지 수신 시 예외 처리
 */

@ExtendWith(MockitoExtension.class)
public class ClientHandlerTest {

    @Mock
    private Socket mockSocket;

    @Mock
    private GameClient mockGameClient;

    private ClientHandler clientHandler;

    @BeforeEach
    void setUp() {
        clientHandler = new ClientHandler(mockSocket, mockGameClient);
    }

    @Test
    void constructor_shouldInitializeWithSocketAndClient() {
        // then
        assertNotNull(clientHandler, "ClientHandler should be created");
    }

    @Test
    void handleMessage_withValidGameStartMessage_shouldNotifyClient() {
        // given
        GameMessage message = new GameMessage(MessageType.GAME_START);

        // when
        clientHandler.handleMessage(message);

        // then
        verify(mockGameClient).onMessageReceived(message);
    }

    @Test
    void handleMessage_withPlayerInputMessage_shouldForwardToClient() {
        // given
        GameMessage message = new GameMessage(MessageType.PLAYER_INPUT);

        // when
        clientHandler.handleMessage(message);

        // then
        verify(mockGameClient).onMessageReceived(message);
    }

    @Test
    void handleMessage_withGameEndMessage_shouldNotifyClient() {
        // given
        GameMessage message = new GameMessage(MessageType.GAME_END);

        // when
        clientHandler.handleMessage(message);

        // then
        verify(mockGameClient).onMessageReceived(message);
    }

    @Test
    void handleMessage_withNullMessage_shouldNotCrash() {
        // when & then
        assertDoesNotThrow(() -> {
            clientHandler.handleMessage(null);
        });
    }

    @Test
    void close_shouldCloseSocket() throws IOException {
        // when
        clientHandler.close();

        // then
        verify(mockSocket).close();
    }

    @Test
    void isRunning_shouldReturnTrueInitially() {
        // when
        boolean running = clientHandler.isRunning();

        // then
        assertTrue(running, "Should be running after creation");
    }

    @Test
    void stop_shouldStopHandler() {
        // when
        clientHandler.stop();
        boolean running = clientHandler.isRunning();

        // then
        assertFalse(running, "Should not be running after stop");
    }
}
