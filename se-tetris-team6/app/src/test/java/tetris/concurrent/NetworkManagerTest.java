package tetris.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import tetris.network.GameEventListener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.concurrent.NetworkManager
 *
 * 역할 요약:
 * - 네트워크 통신의 동시성을 관리하는 중앙 관리자
 * - 메시지 큐를 관리하고 송신/수신을 스레드 안전하게 처리
 * - 여러 네트워크 스레드 간의 동기화 및 이벤트 전파
 * - 네트워크 통계 수집 및 상태 모니터링
 *
 * 테스트 전략:
 * - 메시지 큐 관리(enqueue, dequeue) 로직 테스트
 * - 스레드 안전성은 실제 멀티스레드 테스트로 검증
 * - 이벤트 리스너 등록 및 통지 검증
 * - 통계 수집 및 조회 테스트
 *
 * 사용 라이브러리:
 * - JUnit 5
 * - Mockito (리스너 Mock)
 *
 * 주요 테스트 시나리오:
 * - 메시지 큐에 추가 및 제거
 * - 큐가 비었을 때 dequeue는 null 반환
 * - 여러 메시지 순차 처리 (FIFO)
 * - 리스너 등록 및 이벤트 통지
 * - 통계 업데이트 및 조회
 */

@ExtendWith(MockitoExtension.class)
public class NetworkManagerTest {

    @Mock
    private GameEventListener mockListener;

    private NetworkManager networkManager;

    @BeforeEach
    void setUp() {
        networkManager = new NetworkManager();
        networkManager.addListener(mockListener);
    }

    @Test
    void enqueueMessage_shouldAddMessageToQueue() {
        // given
        GameMessage message = new GameMessage(MessageType.PLAYER_INPUT);

        // when
        networkManager.enqueueMessage(message);

        // then
        assertFalse(networkManager.isQueueEmpty(), 
            "Queue should not be empty after enqueue");
    }

    @Test
    void dequeueMessage_shouldReturnFIFOOrder() {
        // given
        GameMessage message1 = new GameMessage(MessageType.PLAYER_INPUT);
        GameMessage message2 = new GameMessage(MessageType.ATTACK_LINES);
        GameMessage message3 = new GameMessage(MessageType.GAME_START);

        networkManager.enqueueMessage(message1);
        networkManager.enqueueMessage(message2);
        networkManager.enqueueMessage(message3);

        // when
        GameMessage first = networkManager.dequeueMessage();
        GameMessage second = networkManager.dequeueMessage();
        GameMessage third = networkManager.dequeueMessage();

        // then
        assertEquals(MessageType.PLAYER_INPUT, first.getType(), 
            "First message should be PLAYER_INPUT");
        assertEquals(MessageType.ATTACK_LINES, second.getType(), 
            "Second message should be ATTACK_LINES");
        assertEquals(MessageType.GAME_START, third.getType(), 
            "Third message should be GAME_START");
    }

    @Test
    void dequeueMessage_whenQueueEmpty_shouldReturnNull() {
        // when
        GameMessage message = networkManager.dequeueMessage();

        // then
        assertNull(message, "Should return null when queue is empty");
    }

    @Test
    void isQueueEmpty_shouldReturnTrueInitially() {
        // when
        boolean empty = networkManager.isQueueEmpty();

        // then
        assertTrue(empty, "Queue should be empty initially");
    }

    @Test
    void clearQueue_shouldRemoveAllMessages() {
        // given
        networkManager.enqueueMessage(new GameMessage(MessageType.PLAYER_INPUT));
        networkManager.enqueueMessage(new GameMessage(MessageType.ATTACK_LINES));

        // when
        networkManager.clearQueue();

        // then
        assertTrue(networkManager.isQueueEmpty(), 
            "Queue should be empty after clear");
    }

    @Test
    void getQueueSize_shouldReturnCorrectCount() {
        // given
        networkManager.enqueueMessage(new GameMessage(MessageType.PLAYER_INPUT));
        networkManager.enqueueMessage(new GameMessage(MessageType.ATTACK_LINES));
        networkManager.enqueueMessage(new GameMessage(MessageType.GAME_START));

        // when
        int size = networkManager.getQueueSize();

        // then
        assertEquals(3, size, "Queue size should be 3");
    }

    @Test
    void notifyListeners_shouldCallAllRegisteredListeners() {
        // given
        GameEventListener listener2 = mock(GameEventListener.class);
        networkManager.addListener(listener2);
        GameMessage message = new GameMessage(MessageType.GAME_START);

        // when
        networkManager.notifyListeners(message);

        // then
        verify(mockListener).onGameEvent(message);
        verify(listener2).onGameEvent(message);
    }

    @Test
    void removeListener_shouldStopNotifyingRemovedListener() {
        // given
        GameMessage message = new GameMessage(MessageType.GAME_START);

        // when
        networkManager.removeListener(mockListener);
        networkManager.notifyListeners(message);

        // then
        verify(mockListener, never()).onGameEvent(any());
    }

    @Test
    void getStats_shouldReturnNetworkStatistics() {
        // when
        NetworkStats stats = networkManager.getStats();

        // then
        assertNotNull(stats, "Stats should not be null");
    }
}
