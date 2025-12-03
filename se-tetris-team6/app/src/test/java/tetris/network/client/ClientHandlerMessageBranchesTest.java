/**
 * 대상: tetris.network.client.ClientHandler
 *
 * 목적:
 * - handleMessage의 여러 메시지 타입(GAME_START, PLAYER_INPUT, ATTACK_LINES, BOARD_STATE)을 리플렉션으로 호출해
 *   분기 커버리지를 보강한다.
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

class ClientHandlerMessageBranchesTest {

    @Test
    void handleVariousMessages_noThrow() throws Exception {
        GameClient client = mock(GameClient.class, Mockito.withSettings().lenient());
        when(client.isConnected()).thenReturn(true);
        GameStateListener listener = mock(GameStateListener.class, Mockito.withSettings().lenient());
        when(client.getGameStateListener()).thenReturn(listener);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new GameMessage(MessageType.GAME_START, "s", null));
        oout.writeObject(new GameMessage(MessageType.PLAYER_INPUT, "s", null));
        oout.writeObject(new GameMessage(MessageType.ATTACK_LINES, "s", null));
        oout.writeObject(new GameMessage(MessageType.BOARD_STATE, "s", null));
        oout.flush();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream());
        ClientHandler handler = new ClientHandler(in, out, client, new CountDownLatch(0));
        Method handle = ClientHandler.class.getDeclaredMethod("handleMessage", GameMessage.class);
        handle.setAccessible(true);

        while (true) {
            try {
                GameMessage m = (GameMessage) in.readObject();
                assertDoesNotThrow(() -> handle.invoke(handler, m));
            } catch (Exception e) {
                break; // EOF 도달
            }
        }
    }
}
