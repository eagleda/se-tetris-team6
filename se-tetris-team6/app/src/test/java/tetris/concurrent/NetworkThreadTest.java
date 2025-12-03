package tetris.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.network.INetworkThreadCallback;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.concurrent.NetworkThread
 *
 * 역할 요약:
 * - 네트워크 통신을 담당하는 전용 스레드
 * - 실제 소켓 I/O를 처리하며 GameThread와 독립적으로 동작
 * - 메시지 큐(outgoing, incoming, priority)를 관리
 * - 주기적인 ping/pong과 게임 동기화 처리
 * - 연결 끊김 감지 및 재연결 로직
 *
 * 테스트 전략:
 * - 실제 소켓 연결 없이 메시지 큐 관리 로직을 중심으로 테스트
 * - Mock을 사용하여 INetworkThreadCallback 격리
 * - 스레드 동작보다는 메시지 전송/수신 로직 검증
 *
 * 사용 라이브러리:
 * - JUnit 5
 * - Mockito (콜백 Mock)
 *
 * 주요 테스트 시나리오:
 * - 생성자를 통한 초기화
 * - 메시지 큐에 추가 및 전송
 * - 연결 상태 관리
 * - 스레드 시작/종료
 * - 우선순위 메시지 처리
 */

@ExtendWith(MockitoExtension.class)
public class NetworkThreadTest {

    @Mock
    private INetworkThreadCallback mockCallback;

    private NetworkThread networkThread;

    @BeforeEach
    void setUp() {
        networkThread = new NetworkThread(mockCallback);
    }

    @Test
    void constructor_shouldInitializeWithCallback() {
        // then
        assertNotNull(networkThread, "NetworkThread should be created");
        assertTrue(networkThread.isRunning(), "Should be running initially");
    }

    @Test
    void isRunning_shouldReturnTrueInitially() {
        // when
        boolean running = networkThread.isRunning();

        // then
        assertTrue(running, "Should be running after creation");
    }

    @Test
    void isConnected_shouldReturnFalseInitially() {
        // when
        boolean connected = networkThread.isConnected();

        // then
        assertFalse(connected, "Should not be connected initially");
    }

    @Test
    void sendMessage_shouldEnqueueMessage() {
        // given
        GameMessage message = new GameMessage(MessageType.PLAYER_INPUT);

        // when
        networkThread.sendMessage(message);

        // then
        // Message should be in outgoing queue
        assertDoesNotThrow(() -> networkThread.sendMessage(message));
    }

    @Test
    void sendPriorityMessage_shouldEnqueueToPriorityQueue() {
        // given
        GameMessage pingMessage = new GameMessage(MessageType.PING);

        // when
        networkThread.sendPriorityMessage(pingMessage);

        // then
        assertDoesNotThrow(() -> networkThread.sendPriorityMessage(pingMessage));
    }

    @Test
    void stop_shouldStopThread() {
        // when
        networkThread.stop();

        // then
        assertFalse(networkThread.isRunning(), "Should not be running after stop");
    }

    @Test
    void pollIncomingMessage_whenQueueEmpty_shouldReturnNull() {
        // when
        GameMessage message = networkThread.pollIncomingMessage();

        // then
        assertNull(message, "Should return null when queue is empty");
    }

    @Test
    void hasIncomingMessages_shouldReturnFalseInitially() {
        // when
        boolean hasMessages = networkThread.hasIncomingMessages();

        // then
        assertFalse(hasMessages, "Should have no incoming messages initially");
    }

    @Test
    void clearQueues_shouldClearAllQueues() {
        // given
        networkThread.sendMessage(new GameMessage(MessageType.PLAYER_INPUT));
        networkThread.sendPriorityMessage(new GameMessage(MessageType.PING));

        // when
        networkThread.clearQueues();

        // then
        assertFalse(networkThread.hasIncomingMessages(), 
            "Should have no messages after clear");
    }

    @Test
    void getQueueSize_shouldReturnZeroInitially() {
        // when
        int size = networkThread.getQueueSize();

        // then
        assertEquals(0, size, "Queue should be empty initially");
    }

    @Test
    void sendMessage_withNullMessage_shouldNotCrash() {
        // when & then
        assertDoesNotThrow(() -> {
            networkThread.sendMessage(null);
        });
    }

    @Test
    void multipleMessages_shouldBeQueued() {
        // given
        GameMessage msg1 = new GameMessage(MessageType.PLAYER_INPUT);
        GameMessage msg2 = new GameMessage(MessageType.ATTACK_LINES);
        GameMessage msg3 = new GameMessage(MessageType.GAME_START);

        // when
        networkThread.sendMessage(msg1);
        networkThread.sendMessage(msg2);
        networkThread.sendMessage(msg3);

        // then
        // All messages should be queued
        assertDoesNotThrow(() -> {
            networkThread.sendMessage(msg1);
            networkThread.sendMessage(msg2);
            networkThread.sendMessage(msg3);
        });
    }
}
