package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.concurrent.GameEvent
 *
 * 역할 요약:
 * - GameThread가 네트워크/외부로 전달하는 최소 이벤트 DTO.
 *
 * 테스트 전략:
 * - type이 null이면 예외가 발생하는지 확인.
 * - 생성자에 전달한 type과 payload가 그대로 노출되는지 검증.
 */
class GameEventTest {

    @Test
    void nullType_throwsException() {
        assertThrows(NullPointerException.class, () -> new GameEvent(null, null));
    }

    @Test
    void storesTypeAndPayload() {
        Object payload = "data";
        GameEvent event = new GameEvent(GameEvent.Type.LINE_CLEAR, payload);

        assertEquals(GameEvent.Type.LINE_CLEAR, event.getType());
        assertSame(payload, event.getPayload());
    }
}
