/**
 * 대상: tetris.network.client.ClientHandler
 *
 * 목적:
 * - PING 수신 시 PONG 전송 분기와 GAME_END 메시지 포워딩 흐름이 예외 없이 수행되는지 스모크한다.
 * - Mockito 사용 이유: GameClient 협력자를 간단히 스텁하여 네트워크 없이 handleMessage를 실행하기 위함.
 */
package tetris.network.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

class ClientHandlerPingPongTest {

    @Test
    void handlePingAndGameEnd_paths() throws Exception {
        GameClient client = mock(GameClient.class, Mockito.withSettings().lenient());
        when(client.isConnected()).thenReturn(true);
        when(client.getPlayerId()).thenReturn("CLIENT");
        GameStateListener listener = mock(GameStateListener.class, Mockito.withSettings().lenient());
        when(client.getGameStateListener()).thenReturn(listener);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new GameMessage(MessageType.PING, "server", null));
        oout.writeObject(new GameMessage(MessageType.GAME_END, "server", null));
        oout.flush();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream());
        CountDownLatch latch = new CountDownLatch(1);

        ClientHandler handler = new ClientHandler(in, out, client, latch);
        Method handle = ClientHandler.class.getDeclaredMethod("handleMessage", GameMessage.class);
        handle.setAccessible(true);

        assertDoesNotThrow(() -> handle.invoke(handler, (GameMessage) in.readObject())); // PING
        assertDoesNotThrow(() -> handle.invoke(handler, (GameMessage) in.readObject())); // GAME_END
    }
}
