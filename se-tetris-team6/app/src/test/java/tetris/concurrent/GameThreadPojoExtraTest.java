/**
 * 대상: GameThread$LineClearResult 다른 생성자 및 GameEvent 다른 타입 스모크
 */
package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class GameThreadPojoExtraTest {

    @Test
    void lineClearResult_withoutAttackLines() {
        GameThread.LineClearResult r = new GameThread.LineClearResult(0, null, 0);
        assertEquals(0, r.getLinesCleared());
        assertArrayEquals(null, r.getAttackLines());
    }

    @Test
    void gameEvent_otherType() {
        GameEvent ev = new GameEvent(GameEvent.Type.GAME_OVER, 123L);
        assertEquals(GameEvent.Type.GAME_OVER, ev.getType());
        assertEquals(123L, ev.getPayload());
    }
}
