/*
 * 테스트 대상: tetris.concurrent.NetworkThread 및 내부 클래스 NetworkReader
 *
 * 역할 요약:
 * - NetworkThread는 게임 네트워크 메시지를 송수신하는 스레드이며,
 *   내부 NetworkReader는 ObjectInputStream에서 메시지를 읽어 큐에 적재하거나
 *   연결 끊김을 감지합니다.
 *
 * 테스트 전략:
 * - 실제 소켓을 열지 않고, 리플렉션으로 입력 스트림을 주입한 뒤
 *   NetworkReader.run()을 직접 호출해 예외 처리/콜백 흐름을 검증합니다.
 * - AtomicBoolean 플래그(isConnected/isRunning)를 강제로 true로 설정하여
 *   읽기 루프가 한 번 실행되도록 만듭니다.
 *
 * 주요 테스트 시나리오 예시:
 * - 손상된 입력 스트림을 읽을 때 IOException이 발생해 onConnectionLost가 호출되고
 *   콜백의 handleConnectionLost가 한번 호출된다.
 */

package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.network.INetworkThreadCallback;

class NetworkThreadReaderTest {

    @Test
    void networkReader_handlesCorruptedStream_andNotifiesCallback() throws Exception {
        INetworkThreadCallback callback = mock(INetworkThreadCallback.class, Mockito.withSettings().lenient());
        NetworkThread thread = new NetworkThread(callback, "localhost", 0);

        // 강제로 실행/연결 플래그 on
        setAtomicFlag(thread, "isRunning", true);
        setAtomicFlag(thread, "isConnected", true);

        // 손상된 스트림 주입 (헤더 없음 -> StreamCorruptedException)
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        // header only, no objects
        new ObjectOutputStream(bout).flush();
        Field inField = NetworkThread.class.getDeclaredField("inputStream");
        inField.setAccessible(true);
        inField.set(thread, new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray())));

        // 내부 클래스 NetworkReader 인스턴스화 후 run 호출
        Class<?> readerClass = Class.forName("tetris.concurrent.NetworkThread$NetworkReader");
        Constructor<?> ctor = readerClass.getDeclaredConstructor(NetworkThread.class);
        ctor.setAccessible(true);
        Runnable reader = (Runnable) ctor.newInstance(thread);
        assertNotNull(reader);

        reader.run();

        // 연결 끊김 콜백이 호출되었는지 확인
        verify(callback).handleConnectionLost();
    }

    private static void setAtomicFlag(NetworkThread target, String fieldName, boolean value) throws Exception {
        Field f = NetworkThread.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        AtomicBoolean flag = (AtomicBoolean) f.get(target);
        flag.set(value);
    }
}
