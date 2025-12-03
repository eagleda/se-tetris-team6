package tetris.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

/**
 * 대상: tetris.network.server.GameServer
 *
 * 목적:
 * - broadcastMessage(GameMessage) 호출 시 연결된 클라이언트에게 메시지가 전달되는지 검증한다.
 * - checkAndStartIfReady()가 호스트/클라이언트 준비 상태에 따라 GAME_START를 브로드캐스트하는지 확인한다.
 *
 * 주요 시나리오:
 * 1) GAME_END payload를 가진 메시지가 연결된 handler에게 전달된다.
 * 2) hostReady와 clientReady가 true일 때 GAME_START 메시지가 한번 전송된다.
 */
@ExtendWith(MockitoExtension.class)
class GameServerCoverageTest {

    private GameServer server;

    @BeforeEach
    void setUp() {
        server = new GameServer();
    }

    @Test
    void broadcastMessage_sendsToConnectedHandlers() throws Exception {
        ServerHandler handler = mock(ServerHandler.class);
        setConnectedClients(new CopyOnWriteArrayList<>(List.of(handler)));

        GameMessage endMsg = new GameMessage(MessageType.GAME_END, "SERVER", Map.of("winnerId", 1, "loserId", 2));

        server.broadcastMessage(endMsg);

        verify(handler).sendMessage(endMsg);
    }

    @Test
    void checkAndStartIfReady_broadcastsGameStartOnce() throws Exception {
        ServerHandler handler = mock(ServerHandler.class);
        setConnectedClients(new CopyOnWriteArrayList<>(List.of(handler)));
        server.setSelectedGameMode("NORMAL");

        // host and one client ready
        server.setClientReady(handler, true);
        server.setHostReady(true);

        verify(handler).sendMessage(argThat(msg -> {
            assertNotNull(msg);
            assertEquals(MessageType.GAME_START, msg.getType());
            Object payload = msg.getPayload();
            return payload instanceof Map && "NORMAL".equals(((Map<?, ?>) payload).get("mode"));
        }));
    }

    private void setConnectedClients(CopyOnWriteArrayList<ServerHandler> list) throws Exception {
        Field f = GameServer.class.getDeclaredField("connectedClients");
        f.setAccessible(true);
        f.set(server, list);
    }
}
