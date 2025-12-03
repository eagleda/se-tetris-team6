package tetris.concurrent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * 테스트 대상: tetris.concurrent.GameEvent
 *
 * 역할 요약:
 * - 게임 내 이벤트를 표현하는 값 객체 (Value Object)
 * - Type(GENERIC, LINE_CLEAR, ATTACK_RECEIVED, GAME_OVER)과 payload로 구성
 * - GameThread에서 UI, 네트워크 등 다른 시스템으로 게임 이벤트를 전파하는 데 사용
 * - 불변(immutable) 객체로 설계되어 스레드 안전성 보장
 *
 * 테스트 전략:
 * - 생성자를 통한 객체 생성 및 getter 메서드 검증
 * - 각 이벤트 타입별로 올바르게 생성되는지 확인
 * - null payload 처리 확인
 * - 불변성 검증
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 각 이벤트 타입(GENERIC, LINE_CLEAR, ATTACK_RECEIVED, GAME_OVER)으로 생성
 * - payload가 올바르게 저장되고 조회되는지 확인
 * - null 타입으로 생성 시 예외 발생 확인
 * - null payload는 허용되는지 확인
 */

public class GameEventTest {

    @Test
    void constructor_withGenericType_shouldCreateEvent() {
        // given
        String payload = "test message";

        // when
        GameEvent event = new GameEvent(GameEvent.Type.GENERIC, payload);

        // then
        assertEquals(GameEvent.Type.GENERIC, event.getType());
        assertEquals(payload, event.getPayload());
    }

    @Test
    void constructor_withLineClearType_shouldCreateEvent() {
        // given
        Integer linesCleared = 4;

        // when
        GameEvent event = new GameEvent(GameEvent.Type.LINE_CLEAR, linesCleared);

        // then
        assertEquals(GameEvent.Type.LINE_CLEAR, event.getType());
        assertEquals(linesCleared, event.getPayload());
    }

    @Test
    void constructor_withAttackReceivedType_shouldCreateEvent() {
        // given
        String attackData = "attack info";

        // when
        GameEvent event = new GameEvent(GameEvent.Type.ATTACK_RECEIVED, attackData);

        // then
        assertEquals(GameEvent.Type.ATTACK_RECEIVED, event.getType());
        assertEquals(attackData, event.getPayload());
    }

    @Test
    void constructor_withGameOverType_shouldCreateEvent() {
        // given
        Boolean isWinner = true;

        // when
        GameEvent event = new GameEvent(GameEvent.Type.GAME_OVER, isWinner);

        // then
        assertEquals(GameEvent.Type.GAME_OVER, event.getType());
        assertEquals(isWinner, event.getPayload());
    }

    @Test
    void constructor_withNullType_shouldThrowException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            new GameEvent(null, "payload");
        });
    }

    @Test
    void constructor_withNullPayload_shouldAllowNull() {
        // when
        GameEvent event = new GameEvent(GameEvent.Type.GENERIC, null);

        // then
        assertEquals(GameEvent.Type.GENERIC, event.getType());
        assertNull(event.getPayload());
    }

    @Test
    void getType_shouldReturnCorrectType() {
        // given
        GameEvent event = new GameEvent(GameEvent.Type.LINE_CLEAR, 2);

        // when
        GameEvent.Type type = event.getType();

        // then
        assertEquals(GameEvent.Type.LINE_CLEAR, type);
    }

    @Test
    void getPayload_shouldReturnCorrectPayload() {
        // given
        String testPayload = "test data";
        GameEvent event = new GameEvent(GameEvent.Type.GENERIC, testPayload);

        // when
        Object payload = event.getPayload();

        // then
        assertEquals(testPayload, payload);
    }

    @Test
    void eventType_shouldHaveAllExpectedConstants() {
        // when
        GameEvent.Type[] types = GameEvent.Type.values();

        // then
        assertEquals(4, types.length, "Should have 4 event types");
        assertTrue(containsType(types, GameEvent.Type.GENERIC));
        assertTrue(containsType(types, GameEvent.Type.LINE_CLEAR));
        assertTrue(containsType(types, GameEvent.Type.ATTACK_RECEIVED));
        assertTrue(containsType(types, GameEvent.Type.GAME_OVER));
    }

    @Test
    void multipleEvents_shouldBeIndependent() {
        // given
        GameEvent event1 = new GameEvent(GameEvent.Type.LINE_CLEAR, 1);
        GameEvent event2 = new GameEvent(GameEvent.Type.GAME_OVER, true);

        // when & then
        assertNotEquals(event1.getType(), event2.getType());
        assertNotEquals(event1.getPayload(), event2.getPayload());
    }

    private boolean containsType(GameEvent.Type[] types, GameEvent.Type target) {
        for (GameEvent.Type type : types) {
            if (type == target) {
                return true;
            }
        }
        return false;
    }
}
