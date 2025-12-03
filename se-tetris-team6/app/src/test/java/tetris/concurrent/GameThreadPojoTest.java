/**
 * 대상: GameThread의 내부 POJO/레코드류(GameEvent, LineClearResult)
 *
 * 목적:
 * - 단순 게터/생성자 경로를 호출하여 남은 미싱 라인을 메워준다.
 */
package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameThreadPojoTest {

    @Test
    void gameEvent_exposesTypeAndPayload() {
        GameEvent ev = new GameEvent(GameEvent.Type.GENERIC, "payload");
        assertEquals(GameEvent.Type.GENERIC, ev.getType());
        assertEquals("payload", ev.getPayload());
    }

    @Test
    void lineClearResult_exposesFields() {
        GameThread.LineClearResult r = new GameThread.LineClearResult(2, null, 100);
        assertEquals(2, r.getLinesCleared());
        assertEquals(100, r.getPoints());
        assertArrayEquals(null, r.getAttackLines());
    }

    @Test
    void gameEvent_tickType_hasData() {
        GameEvent ev = new GameEvent(GameEvent.Type.LINE_CLEAR, 42L);
        assertEquals(GameEvent.Type.LINE_CLEAR, ev.getType());
        assertEquals(42L, ev.getPayload());
    }
}
