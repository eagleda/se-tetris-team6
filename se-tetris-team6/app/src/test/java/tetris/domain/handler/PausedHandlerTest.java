package tetris.domain.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/*
 * 테스트 대상: tetris.domain.handler.PausedHandler
 *
 * 역할 요약:
 * - PAUSED 상태에서 게임 시계를 멈추고 일시정지 오버레이를 표시하며, 종료 시 시계를 재개한다.
 *
 * 테스트 전략:
 * - enter가 pauseClock/showPauseOverlay를 호출하고, exit가 hidePauseOverlay/resumeClock을 호출하는지 검증.
 * - getState가 PAUSED를 반환하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - enter_invokesPauseAndShowOverlay
 * - exit_invokesHideOverlayAndResume
 */
@ExtendWith(MockitoExtension.class)
class PausedHandlerTest {

    @Mock GameModel model;
    PausedHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PausedHandler();
    }

    @Test
    void enter_invokesPauseAndShowOverlay() {
        handler.enter(model);
        verify(model).pauseClock();
        verify(model).showPauseOverlay();
    }

    @Test
    void exit_invokesHideOverlayAndResume() {
        handler.exit(model);
        verify(model).hidePauseOverlay();
        verify(model).resumeClock();
    }

    @Test
    void getState_returnsPaused() {
        assertEquals(GameState.PAUSED, handler.getState());
    }
}
