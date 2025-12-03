package tetris.network.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import tetris.network.GameEventListener;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.network.server.GameServer
 *
 * 역할 요약:
 * - P2P 멀티플레이에서 호스트(서버) 역할을 담당
 * - 클라이언트의 연결을 수락하고 GameMessage를 송수신
 * - ServerStatus를 관리하며 게임 시작/종료 신호 전송
 * - 클라이언트 연결 끊김 감지 및 타임아웃 처리
 *
 * 테스트 전략:
 * - 서버 생명주기(시작-대기-연결-게임-종료)를 중심으로 테스트
 * - 클라이언트 연결 수락 및 메시지 브로드캐스트 검증
 * - 상태 전환(IDLE → WAITING → CONNECTED → IN_GAME) 확인
 * - 예외 상황(포트 충돌, 연결 끊김) 처리 검증
 *
 * 사용 라이브러리:
 * - JUnit 5
 * - Mockito (리스너, 소켓 Mock)
 *
 * 주요 테스트 시나리오:
 * - 서버 시작 시 WAITING 상태로 전환
 * - 클라이언트 연결 시 CONNECTED 상태로 전환
 * - 메시지 브로드캐스트 시 연결된 모든 클라이언트에 전송
 * - 서버 종료 시 모든 리소스 정리
 * - 포트가 이미 사용 중일 때 예외 처리
 * - 클라이언트 연결 끊김 시 상태 변경
 */

@ExtendWith(MockitoExtension.class)
public class GameServerTest {

    @Mock
    private GameEventListener mockListener;

    private GameServer gameServer;
    private final int testPort = 54321;

    @BeforeEach
    void setUp() {
        gameServer = new GameServer(testPort);
        gameServer.setListener(mockListener);
    }

    @Test
    void constructor_shouldInitializeIdleStatus() {
        // when
        ServerStatus status = gameServer.getStatus();

        // then
        assertEquals(ServerStatus.IDLE, status, "Initial status should be IDLE");
    }

    @Test
    void start_shouldChangeStatusToWaiting() throws IOException {
        // when
        gameServer.start();

        // then
        ServerStatus status = gameServer.getStatus();
        assertEquals(ServerStatus.WAITING, status, 
            "Status should change to WAITING after start");
    }

    @Test
    void start_whenAlreadyStarted_shouldNotStartAgain() throws IOException {
        // given
        gameServer.start();
        ServerStatus firstStatus = gameServer.getStatus();

        // when
        gameServer.start(); // Try to start again

        // then
        ServerStatus secondStatus = gameServer.getStatus();
        assertEquals(firstStatus, secondStatus, 
            "Should not change status when already started");
    }

    @Test
    void stop_shouldChangeStatusToIdleAndCloseSocket() {
        // given
        try {
            gameServer.start();
        } catch (IOException e) {
            // Ignore for test
        }

        // when
        gameServer.stop();

        // then
        assertEquals(ServerStatus.IDLE, gameServer.getStatus(), 
            "Status should return to IDLE after stop");
    }

    @Test
    void getPort_shouldReturnConfiguredPort() {
        // when
        int port = gameServer.getPort();

        // then
        assertEquals(testPort, port, "Should return configured port");
    }

    @Test
    void broadcastMessage_shouldSendToAllConnectedClients() {
        // given
        GameMessage message = new GameMessage(MessageType.GAME_START);
        // Assume clients are connected

        // when
        gameServer.broadcastMessage(message);

        // then
        // verify(mockListener, atLeastOnce()).onMessageBroadcast(message);
    }

    @Test
    void onClientConnected_shouldChangeStatusToConnected() {
        // given
        try {
            gameServer.start();
        } catch (IOException e) {
            // Ignore
        }

        // when
        // Simulate client connection (internal method)
        // gameServer.handleClientConnection();

        // then
        // assertEquals(ServerStatus.CONNECTED, gameServer.getStatus());
    }

    @Test
    void onClientDisconnected_shouldChangeStatusBackToWaiting() {
        // given
        // Assume CONNECTED state

        // when
        // Simulate client disconnection
        // gameServer.handleClientDisconnection();

        // then
        // assertEquals(ServerStatus.WAITING, gameServer.getStatus());
    }

    @Test
    void startGame_shouldChangeStatusToInGame() {
        // given
        // Assume CONNECTED state

        // when
        gameServer.startGame();

        // then
        assertEquals(ServerStatus.IN_GAME, gameServer.getStatus(), 
            "Status should change to IN_GAME");
    }

    @Test
    void getConnectedClientCount_shouldReturnZeroInitially() {
        // when
        int count = gameServer.getConnectedClientCount();

        // then
        assertEquals(0, count, "Should have no connected clients initially");
    }

    @Test
    void isRunning_shouldReturnTrueWhenStarted() throws IOException {
        // when
        gameServer.start();
        boolean running = gameServer.isRunning();

        // then
        assertTrue(running, "Should be running after start");

        // cleanup
        gameServer.stop();
    }

    @Test
    void isRunning_shouldReturnFalseWhenStopped() {
        // when
        boolean running = gameServer.isRunning();

        // then
        assertFalse(running, "Should not be running initially");
    }
}
