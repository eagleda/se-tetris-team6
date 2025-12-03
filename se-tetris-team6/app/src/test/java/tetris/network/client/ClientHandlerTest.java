package tetris.network.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

/*
 * 테스트 대상: tetris.network.client.ClientHandler
 *
 * 역할 요약:
 * - 클라이언트 측에서 ObjectInput/OutputStream을 통해 서버와 메시지를 주고받는 Runnable.
 * - 수신 메시지를 GameClient에 위임하고, PING/PONG, 스냅샷 등 네트워크 흐름을 담당한다.
 *
 * 테스트 전략:
 * - 공개 API 중심: 생성자 초기화, sendMessage가 writeObject/flush를 호출하는지 검증.
 * - 예외 상황: outputStream이 null일 때 sendMessage가 NPE 없이 넘어가는지 확인.
 * - 상태: 초기 latency 값이 0인지 확인.
 *
 * - 사용 라이브러리: JUnit 5, Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ClientHandlerTest {

    @Mock ObjectInputStream input;
    @Mock ObjectOutputStream output;
    @Mock GameClient client;
    @Mock CountDownLatch latch;

    private ClientHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClientHandler(input, output, client, latch);
    }

    @Test
    void constructor_initializesHandler() {
        assertNotNull(handler);
        assertEquals(0, handler.getLatency());
    }

    @Test
    void sendMessage_writesObjectAndFlushes() throws Exception {
        GameMessage msg = new GameMessage(MessageType.PING, "client", null);
        handler.sendMessage(msg);

        verify(output).writeObject(msg);
        verify(output).flush();
    }

    @Test
    void sendMessage_withNullOutputStream_doesNotThrow() {
        GameMessage msg = new GameMessage(MessageType.PING, "client", null);
        ClientHandler local = new ClientHandler(input, null, client, latch);

        assertDoesNotThrow(() -> local.sendMessage(msg));
    }
}
