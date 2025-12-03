/**
 * 대상: tetris.concurrent.NetworkThread (우선순위 큐 처리)
 *
 * 목적:
 * - processPriorityMessages가 PING 수신 시 PONG을 큐에 넣고, PONG 수신 시 지연시간을 계산하는 분기를 커버한다.
 *
 * 주요 시나리오:
 * 1) priorityQueue에 PING을 넣어 호출하면 PONG이 소진되고 currentLatency가 0 이상으로 설정된다.
 */
package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import tetris.network.INetworkThreadCallback;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

class NetworkThreadPriorityTest {

    @Test
    void processPriorityMessages_handlesPingPong() throws Exception {
        INetworkThreadCallback cb = new INetworkThreadCallback() {
            @Override public void handleReceivedMessage(GameMessage message) {}
            @Override public void handleConnectionEstablished() {}
            @Override public void handleConnectionLost() {}
            @Override public void handleLatencyWarning(long latency) {}
            @Override public void handleNetworkError(Exception error) {}
        };
        NetworkThread thread = new NetworkThread(cb, "localhost", 1);

        // isConnected=true 로 만들어 재연결 루프를 건너뛴다.
        Field connected = NetworkThread.class.getDeclaredField("isConnected");
        connected.setAccessible(true);
        ((AtomicBoolean) connected.get(thread)).set(true);

        // priorityQueue에 PING 삽입
        Field pqField = NetworkThread.class.getDeclaredField("priorityQueue");
        pqField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.BlockingQueue<GameMessage> pq =
                (java.util.concurrent.BlockingQueue<GameMessage>) pqField.get(thread);
        pq.offer(new GameMessage(MessageType.PING, "server", System.currentTimeMillis()));

        // private processPriorityMessages 호출
        Method m = NetworkThread.class.getDeclaredMethod("processPriorityMessages");
        m.setAccessible(true);
        m.invoke(thread);

        // 호출 후 큐가 비어 있고 latency가 0 이상인지 확인
        assertTrue(pq.isEmpty());
        assertTrue(thread.getCurrentLatency() >= 0);
    }
}
