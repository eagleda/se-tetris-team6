package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.protocol.InputType
 *
 * 역할 요약:
 * - 네트워크 입력 메시지에서 사용되는 입력 종류를 정의하는 열거형.
 *
 * 테스트 전략:
 * - 정의된 상수가 누락 없이 포함되어 있는지 확인한다.
 */
class InputTypeTest {

    @Test
    void containsAllExpectedValues() {
        EnumSet<InputType> all = EnumSet.allOf(InputType.class);
        assertEquals(8, all.size());
        assertTrue(all.contains(InputType.MOVE_LEFT));
        assertTrue(all.contains(InputType.MOVE_RIGHT));
        assertTrue(all.contains(InputType.SOFT_DROP));
        assertTrue(all.contains(InputType.ROTATE));
        assertTrue(all.contains(InputType.ROTATE_CCW));
        assertTrue(all.contains(InputType.HARD_DROP));
        assertTrue(all.contains(InputType.HOLD));
        assertTrue(all.contains(InputType.PAUSE));
    }
}
