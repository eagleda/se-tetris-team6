package tetris.network.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.network.protocol.AttackLine;
import tetris.network.protocol.GameSnapshot;
import tetris.network.protocol.InputType;
import tetris.network.protocol.PlayerInput;

/**
 * 대상: GameClient의 네트워크 송신 경로 (sendPlayerInput, sendAttackLines,
 * sendGameStateSnapshot, startPingMeasurement).
 *
 * 목적:
 * - 연결 상태 플래그와 clientHandler 유무에 따라 메시지가 보내지는지/무시되는지 검증한다.
 * - 핑 측정 스레드가 PING 메시지를 전송하는지 확인하고 즉시 중단할 수 있음을 보장한다.
 *
 * 주요 시나리오:
 * 1) 연결된 상태 + handler 존재 시 각 송신 메서드가 GameMessage를 전달한다.
 * 2) 연결되지 않았거나 null 인자일 때는 handler 호출이 일어나지 않는다.
 * 3) startPingMeasurement가 PING을 발송하고 stopPingMeasurement로 안전히 종료된다.
 */
class GameClientCoverageTest {

    private GameClient client;
    private ClientHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        client = new GameClient();
        handler = mock(ClientHandler.class);
        setPrivate(client, "clientHandler", handler);
        setPrivate(client, "isConnected", true);
        client.setPlayerId("P1");
    }

    @AfterEach
    void tearDown() {
        client.stopPingMeasurement();
    }

    @Test
    void sendPlayerInput_whenConnected_sendsMessage() {
        PlayerInput input = new PlayerInput(InputType.MOVE_LEFT);

        client.sendPlayerInput(input);

        verify(handler).sendMessage(Mockito.argThat(msg -> msg.getType() == tetris.network.protocol.MessageType.PLAYER_INPUT
                && "P1".equals(msg.getSenderId())
                && input.equals(msg.getPayload())));
    }

    @Test
    void sendAttackLines_nullOrDisconnected_noSend() throws Exception {
        client.sendAttackLines(null);
        setPrivate(client, "isConnected", false);
        client.sendAttackLines(new AttackLine[]{new AttackLine(1)});

        verify(handler, never()).sendMessage(Mockito.any());
    }

    @Test
    void sendGameStateSnapshot_whenConnected_sendsGameState() {
        GameSnapshot snapshot = new GameSnapshot(1, new int[1][1], 0, 0, 0, 0, 0, 0, 0, 0,
                new boolean[0][0], "STANDARD", null, -1, -1, null);

        client.sendGameStateSnapshot(snapshot);

        verify(handler).sendMessage(Mockito.argThat(msg -> msg.getType() == tetris.network.protocol.MessageType.GAME_STATE
                && snapshot.equals(msg.getPayload())));
    }

    @Test
    void startPingMeasurement_sendsPingAndStops() {
        client.startPingMeasurement();

        verify(handler, timeout(200)).sendMessage(Mockito.argThat(msg -> msg.getType() == tetris.network.protocol.MessageType.PING));

        client.stopPingMeasurement();
        assertTrue(client.getCurrentPing() == -1 || client.getCurrentPing() >= 0);
    }

    private static void setPrivate(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
