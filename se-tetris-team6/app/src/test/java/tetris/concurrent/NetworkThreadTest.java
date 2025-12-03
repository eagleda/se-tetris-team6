package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tetris.network.INetworkThreadCallback;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

/*
 * 테스트 대상: tetris.concurrent.NetworkThread 및 내부 NetworkReader
 *
 * 역할 요약:
 * - 네트워크 송수신 큐, 콜백, 상태 플래그를 관리하는 스레드 객체.
 *
 * 테스트 전략:
 * - 콜백/호스트/포트 설정으로 생성이 되는지 확인.
 * - sendMessage/priorityMessage 호출이 예외 없이 큐에 추가되는지 확인.
 * - shutdown이 상태 플래그를 false로 만들고 정리 메서드가 예외 없이 호출되는지 확인.
 * - 내부 NetworkReader 인스턴스 생성 가능 여부 확인(실제 소켓 I/O는 실행하지 않음).
 */
class NetworkThreadTest {

    private static final class DummyCallback implements INetworkThreadCallback {
        @Override public void handleReceivedMessage(GameMessage message) {}
        @Override public void handleConnectionEstablished() {}
        @Override public void handleConnectionLost() {}
        @Override public void handleLatencyWarning(long latency) {}
        @Override public void handleNetworkError(Exception error) {}
    }

    @Test
    void constructAndQueueMessages() {
        NetworkThread thread = new NetworkThread(new DummyCallback(), "localhost", 1);
        assertNotNull(thread);

        thread.sendMessage(new GameMessage(MessageType.PING, "me", "payload"));
        thread.sendPriorityMessage(new GameMessage(MessageType.ERROR, "me", "err"));

        assertFalse(thread.isConnected()); // 아직 연결 시도 전
        thread.shutdown();
        assertFalse(thread.isConnected());
    }

    @Test
    void createNetworkReaderInstance() throws Exception {
        NetworkThread thread = new NetworkThread(new DummyCallback(), "localhost", 1);
        // 리플렉션으로 private 내부 클래스 인스턴스화
        Class<?> readerClass = Class.forName("tetris.concurrent.NetworkThread$NetworkReader");
        java.lang.reflect.Constructor<?> ctor = readerClass.getDeclaredConstructor(NetworkThread.class);
        ctor.setAccessible(true);
        Object reader = ctor.newInstance(thread);
        assertNotNull(reader);
    }
}
