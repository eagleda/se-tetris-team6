/**
 * 대상: tetris.concurrent.NetworkThread$NetworkReader
 *
 * 목적:
 * - PING 메시지가 priorityQueue로, 기타 메시지가 incomingQueue로 분배되는지 확인해 30%대 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) PING → priorityQueue
 * 2) GAME_START → incomingQueue
 *
 * Mockito 불사용: 더미 콜백과 직렬화된 입력 스트림을 사용한다.
 */
package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import tetris.network.INetworkThreadCallback;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

class NetworkThreadReaderBranchTest {

    private static class DummyCallback implements INetworkThreadCallback {
        @Override public void handleReceivedMessage(tetris.network.protocol.GameMessage message) {}
        @Override public void handleConnectionEstablished() {}
        @Override public void handleConnectionLost() {}
        @Override public void handleLatencyWarning(long latency) {}
        @Override public void handleNetworkError(Exception error) {}
    }

    @Test
    void priorityAndIncomingQueues_receiveMessages() throws Exception {
        NetworkThread thread = new NetworkThread(new DummyCallback(), "localhost", 1);

        setFlag(thread, "isRunning", true);
        setFlag(thread, "isConnected", true);

        // 직렬화된 PING, GAME_START 메시지 준비
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new GameMessage(MessageType.PING, "s", 123L));
        oout.writeObject(new GameMessage(MessageType.GAME_START, "s", null));
        oout.flush();

        Field inField = NetworkThread.class.getDeclaredField("inputStream");
        inField.setAccessible(true);
        inField.set(thread, new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray())));

        // 내부 클래스 인스턴스화 후 run 실행
        Class<?> readerClass = Class.forName("tetris.concurrent.NetworkThread$NetworkReader");
        Constructor<?> ctor = readerClass.getDeclaredConstructor(NetworkThread.class);
        ctor.setAccessible(true);
        Runnable reader = (Runnable) ctor.newInstance(thread);
        reader.run();

        Field priQ = NetworkThread.class.getDeclaredField("priorityQueue");
        Field incQ = NetworkThread.class.getDeclaredField("incomingQueue");
        priQ.setAccessible(true);
        incQ.setAccessible(true);
        java.util.concurrent.BlockingQueue<?> pq = (java.util.concurrent.BlockingQueue<?>) priQ.get(thread);
        java.util.concurrent.BlockingQueue<?> iq = (java.util.concurrent.BlockingQueue<?>) incQ.get(thread);

        assertEquals(1, pq.size(), "PING은 priorityQueue로");
        assertEquals(1, iq.size(), "GAME_START는 incomingQueue로");
    }

    private static void setFlag(NetworkThread t, String field, boolean value) throws Exception {
        Field f = NetworkThread.class.getDeclaredField(field);
        f.setAccessible(true);
        AtomicBoolean ab = (AtomicBoolean) f.get(t);
        ab.set(value);
    }
}
