package tetris.domain.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tetris.domain.model.GameState;

/*
 * 테스트 대상: tetris.domain.handler.MenuHandler
 *
 * 역할 요약:
 * - 메뉴 상태를 표현하는 핸들러로, 현재 구현은 no-op이다.
 *
 * 테스트 전략:
 * - getState가 MENU를 반환하는지 확인.
 * - enter/update가 예외 없이 동작하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - getState_returnsMenu
 * - hooks_doNotThrow
 */
class MenuHandlerTest {

    @Test
    void getState_returnsMenu() {
        MenuHandler handler = new MenuHandler();
        assertEquals(GameState.MENU, handler.getState());
    }

    @Test
    void hooks_doNotThrow() {
        MenuHandler handler = new MenuHandler();
        assertDoesNotThrow(() -> {
            handler.enter(null);
            handler.update(null);
        });
    }
}
