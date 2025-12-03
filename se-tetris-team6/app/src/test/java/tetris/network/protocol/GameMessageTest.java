package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.protocol.GameMessage
 *
 * 역할 요약:
 * - 메시지 타입/발신자/페이로드/타임스탬프/시퀀스를 포함하는 네트워크 전송 DTO.
 * - 시퀀스는 AtomicInteger로 단조 증가한다.
 *
 * 테스트 전략:
 * - 생성 시 전달한 타입/발신자/페이로드가 그대로 노출되는지 확인.
 * - 시퀀스 번호가 단조 증가하는지 확인.
 * - equals는 동일 인스턴스에 대해서만 true임을 간단히 검증.
 */
class GameMessageTest {

    @Test
    void fieldsAreStoredAndExposed() {
        Object payload = new PlayerInput(InputType.MOVE_LEFT);
        GameMessage msg = new GameMessage(MessageType.PLAYER_INPUT, "P1", payload);

        assertEquals(MessageType.PLAYER_INPUT, msg.getType());
        assertEquals("P1", msg.getSenderId());
        assertSame(payload, msg.getPayload());
        assertTrue(msg.getTimestamp() > 0);
    }

    @Test
    void sequenceNumberIncrementsMonotonically() {
        GameMessage first = new GameMessage(MessageType.PING, "A", null);
        GameMessage second = new GameMessage(MessageType.PING, "A", null);

        assertTrue(second.getSequenceNumber() > first.getSequenceNumber());
    }

    @Test
    void equalsIsReferenceEqualForSameInstance() {
        GameMessage msg = new GameMessage(MessageType.GAME_START, "server", null);
        assertTrue(msg.equals(msg));
    }
}
