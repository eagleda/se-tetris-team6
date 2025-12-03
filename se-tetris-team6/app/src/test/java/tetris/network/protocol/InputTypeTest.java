package tetris.network.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * 테스트 대상: tetris.network.protocol.InputType
 *
 * 역할 요약:
 * - 플레이어 입력 타입을 정의하는 enum
 * - MOVE_LEFT, MOVE_RIGHT, SOFT_DROP, ROTATE, ROTATE_CCW, HARD_DROP, HOLD, PAUSE 정의
 * - PlayerInput 클래스에서 사용되어 네트워크로 전송됨
 * - 게임 입력 처리 로직의 타입 안전성 보장
 *
 * 테스트 전략:
 * - Enum 타입의 기본 동작 검증
 * - 모든 입력 타입이 정의되어 있는지 확인
 * - values(), valueOf() 메서드 정상 동작 확인
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 8가지 입력 타입이 모두 정의되어 있는지 확인
 * - valueOf()로 문자열을 통해 enum을 가져올 수 있는지 확인
 * - values()가 모든 입력 타입을 반환하는지 확인
 */

public class InputTypeTest {

    @Test
    void values_shouldReturnAllInputTypes() {
        // when
        InputType[] types = InputType.values();

        // then
        assertEquals(8, types.length, "Should have 8 input types");
        assertTrue(containsType(types, InputType.MOVE_LEFT));
        assertTrue(containsType(types, InputType.MOVE_RIGHT));
        assertTrue(containsType(types, InputType.SOFT_DROP));
        assertTrue(containsType(types, InputType.ROTATE));
        assertTrue(containsType(types, InputType.ROTATE_CCW));
        assertTrue(containsType(types, InputType.HARD_DROP));
        assertTrue(containsType(types, InputType.HOLD));
        assertTrue(containsType(types, InputType.PAUSE));
    }

    @Test
    void valueOf_shouldReturnCorrectInputType() {
        // when & then
        assertEquals(InputType.MOVE_LEFT, InputType.valueOf("MOVE_LEFT"));
        assertEquals(InputType.MOVE_RIGHT, InputType.valueOf("MOVE_RIGHT"));
        assertEquals(InputType.SOFT_DROP, InputType.valueOf("SOFT_DROP"));
        assertEquals(InputType.ROTATE, InputType.valueOf("ROTATE"));
        assertEquals(InputType.ROTATE_CCW, InputType.valueOf("ROTATE_CCW"));
        assertEquals(InputType.HARD_DROP, InputType.valueOf("HARD_DROP"));
        assertEquals(InputType.HOLD, InputType.valueOf("HOLD"));
        assertEquals(InputType.PAUSE, InputType.valueOf("PAUSE"));
    }

    @Test
    void valueOf_withInvalidName_shouldThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            InputType.valueOf("INVALID_INPUT");
        });
    }

    @Test
    void enumConstantsShouldBeUnique() {
        // given
        InputType[] types = InputType.values();

        // when & then
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                assertNotEquals(types[i], types[j], 
                    "Each input type should be unique");
            }
        }
    }

    @Test
    void enumName_shouldMatchConstantName() {
        // when & then
        assertEquals("MOVE_LEFT", InputType.MOVE_LEFT.name());
        assertEquals("MOVE_RIGHT", InputType.MOVE_RIGHT.name());
        assertEquals("SOFT_DROP", InputType.SOFT_DROP.name());
        assertEquals("ROTATE", InputType.ROTATE.name());
        assertEquals("ROTATE_CCW", InputType.ROTATE_CCW.name());
        assertEquals("HARD_DROP", InputType.HARD_DROP.name());
        assertEquals("HOLD", InputType.HOLD.name());
        assertEquals("PAUSE", InputType.PAUSE.name());
    }

    @Test
    void movementInputs_shouldExist() {
        // when & then
        assertNotNull(InputType.MOVE_LEFT, "MOVE_LEFT should exist");
        assertNotNull(InputType.MOVE_RIGHT, "MOVE_RIGHT should exist");
        assertNotNull(InputType.SOFT_DROP, "SOFT_DROP should exist");
        assertNotNull(InputType.HARD_DROP, "HARD_DROP should exist");
    }

    @Test
    void rotationInputs_shouldExist() {
        // when & then
        assertNotNull(InputType.ROTATE, "ROTATE should exist");
        assertNotNull(InputType.ROTATE_CCW, "ROTATE_CCW should exist");
    }

    @Test
    void specialInputs_shouldExist() {
        // when & then
        assertNotNull(InputType.HOLD, "HOLD should exist");
        assertNotNull(InputType.PAUSE, "PAUSE should exist");
    }

    private boolean containsType(InputType[] types, InputType target) {
        for (InputType type : types) {
            if (type == target) {
                return true;
            }
        }
        return false;
    }
}
