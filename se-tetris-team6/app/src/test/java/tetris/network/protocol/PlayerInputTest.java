package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.protocol.PlayerInput
 *
 * 역할 요약:
 * - 플레이어 입력을 네트워크로 전달하기 위한 직렬화 레코드.
 *
 * 테스트 전략:
 * - 생성 시 전달한 InputType이 그대로 노출되는지 확인.
 * - equals/hashCode가 InputType 기준으로 동작하는지 간단히 검증.
 */
class PlayerInputTest {

    @Test
    void storesInputType() {
        PlayerInput input = new PlayerInput(InputType.MOVE_LEFT);
        assertEquals(InputType.MOVE_LEFT, input.inputType());
    }

    @Test
    void equalityBasedOnInputType() {
        PlayerInput a = new PlayerInput(InputType.HARD_DROP);
        PlayerInput b = new PlayerInput(InputType.HARD_DROP);
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
    }
}
