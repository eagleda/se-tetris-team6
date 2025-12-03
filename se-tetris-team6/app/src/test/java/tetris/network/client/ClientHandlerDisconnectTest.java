/**
 * 대상: tetris.network.client.ClientHandler (DISCONNECT 분기)
 *
 * 목적:
 * - 서버로부터 DISCONNECT 메시지를 받을 때 client.disconnect가 호출되는지 확인해 누락된 분기를 보강한다.
 *
 * 주요 시나리오:
 * 1) handleMessage에 DISCONNECT 메시지를 전달하면 client.disconnect가 1회 호출된다.
 */
package tetris.network.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

class ClientHandlerDisconnectTest {

    @Test
    void handleDisconnect_callsClientDisconnect() throws Exception {
        GameClient client = Mockito.mock(GameClient.class, Mockito.withSettings().lenient());
        when(client.isConnected()).thenReturn(true);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new GameMessage(MessageType.DISCONNECT, "server", null));
        oout.flush();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream());
        ClientHandler handler = new ClientHandler(in, out, client, new CountDownLatch(0));

        Method handle = ClientHandler.class.getDeclaredMethod("handleMessage", GameMessage.class);
        handle.setAccessible(true);
        handle.invoke(handler, (GameMessage) in.readObject());

        verify(client).disconnect();
    }
}
