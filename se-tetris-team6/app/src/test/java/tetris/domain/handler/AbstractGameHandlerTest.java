package tetris.domain.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tetris.domain.model.GameState;

/*
 * 테스트 대상: tetris.domain.handler.AbstractGameHandler
 *
 * 역할 요약:
 * - GameHandler의 공통 보일러플레이트를 제공하며, 상태(GameState)를 보관한다.
 * - enter/update/exit 기본 구현은 no-op이며, getState만 구체 구현을 제공한다.
 *
 * 테스트 전략:
 * - 상태가 생성자 인자를 그대로 반환하는지 확인.
 * - 기본 enter/update/exit가 예외 없이 동작하는지 검증.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - getState_returnsProvidedState
 * - defaultHooks_doNothing
 */
class AbstractGameHandlerTest {

    @Test
    void getState_returnsProvidedState() {
        AbstractGameHandler handler = new DummyHandler(GameState.MENU);
        assertEquals(GameState.MENU, handler.getState());
    }

    @Test
    void defaultHooks_doNothing() {
        AbstractGameHandler handler = new DummyHandler(GameState.PLAYING);
        assertDoesNotThrow(() -> {
            handler.enter(null);
            handler.update(null);
            handler.exit(null);
        });
    }

    private static class DummyHandler extends AbstractGameHandler {
        DummyHandler(GameState state) { super(state); }
    }
}
