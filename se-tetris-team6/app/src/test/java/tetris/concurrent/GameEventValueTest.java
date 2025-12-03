/**
 * 대상: tetris.concurrent.GameEvent, GameEvent.Type
 *
 * 목적:
 * - 이벤트 객체의 타입/페이로드 저장 동작을 검증해 내부 클래스(GameEvent, GameEvent.Type) 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) 생성 시 전달한 타입/페이로드가 그대로 보존되는지 확인
 * 2) GENERIC 타입 등 enum 값이 null 없이 접근 가능한지 확인
 */
package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class GameEventValueTest {

    @Test
    void constructor_storesTypeAndPayload() {
        String payload = "hello";
        GameEvent event = new GameEvent(GameEvent.Type.GENERIC, payload);

        assertEquals(GameEvent.Type.GENERIC, event.getType());
        assertEquals(payload, event.getPayload());
        assertNotNull(GameEvent.Type.GENERIC);
        assertNotNull(GameEvent.Type.LINE_CLEAR);
    }
}
