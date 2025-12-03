package tetris.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.model.InputState
 *
 * 역할 요약:
 * - 이동/소프트드랍 같은 지속 입력과 회전/하드드랍/홀드 같은 1회성 입력을 분리해 보관한다.
 * - pop 메서드로 1회성 입력을 소비하면 내부 플래그를 자동으로 리셋한다.
 *
 * 테스트 전략:
 * - 지속 입력 플래그: setter/getter가 상태를 유지하는지 확인.
 * - 1회성 입력: press/pop 호출 시 true를 반환하고, 두 번째 pop 이후엔 false가 되는지 검증.
 * - clearOneShotInputs로 모든 1회성 플래그가 리셋되는지 확인.
 *
 * - 사용 라이브러리: JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - setLeft/right/softDrop가 상태를 그대로 노출한다.
 * - pressRotateCW/popRotateCW 호출 시 한 번만 true, 이후 false.
 * - clearOneShotInputs 호출 후 모든 1회성 입력이 false.
 */
class InputStateTest {

    private InputState state;

    @BeforeEach
    void setUp() {
        state = new InputState();
    }

    @Test
    void persistentInputs_holdTheirValues() {
        state.setLeft(true);
        state.setRight(false);
        state.setSoftDrop(true);

        assertTrue(state.isLeft());
        assertFalse(state.isRight());
        assertTrue(state.isSoftDrop());
    }

    @Test
    void oneShotInputs_returnTrueOnce_thenReset() {
        state.pressRotateCW();
        state.pressRotateCCW();
        state.pressHardDrop();
        state.pressHold();

        assertTrue(state.popRotateCW());
        assertTrue(state.popRotateCCW());
        assertTrue(state.popHardDrop());
        assertTrue(state.popHold());

        // second pop -> all false
        assertFalse(state.popRotateCW());
        assertFalse(state.popRotateCCW());
        assertFalse(state.popHardDrop());
        assertFalse(state.popHold());
    }

    @Test
    void clearOneShotInputs_resetsAllTransientFlags() {
        state.pressRotateCW();
        state.pressHardDrop();

        state.clearOneShotInputs();

        assertFalse(state.popRotateCW());
        assertFalse(state.popHardDrop());
        assertFalse(state.popRotateCCW());
        assertFalse(state.popHold());
    }
}
