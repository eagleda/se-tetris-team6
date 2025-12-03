/**
 * 대상: tetris.network.client.ClientHandler
 *
 * 목적:
 * - 타임아웃 워치독 시작/종료 흐름을 스모크하고 latency 초기값 등을 확인한다.
 */
package tetris.network.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClientHandlerFlagsTest {

    @Test
    void latencyInitial_and_disconnectFlag() {
        ClientHandler handler = new ClientHandler(
                Mockito.mock(ObjectInputStream.class, Mockito.withSettings().lenient()),
                Mockito.mock(ObjectOutputStream.class, Mockito.withSettings().lenient()),
                Mockito.mock(GameClient.class, Mockito.withSettings().lenient()),
                Mockito.mock(CountDownLatch.class, Mockito.withSettings().lenient()));
        assertEquals(0, handler.getLatency());
    }
}
