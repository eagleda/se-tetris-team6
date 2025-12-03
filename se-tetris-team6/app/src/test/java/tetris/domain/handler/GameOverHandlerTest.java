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
 * 테스트 대상: tetris.domain.handler.GameOverHandler
 *
 * 역할 요약:
 * - GAME_OVER 상태 진입 시 시계 정지, 최종 점수 계산, 게임오버 화면 표시를 수행한다.
 *
 * 테스트 전략:
 * - enter 호출 시 GameModel.pauseClock/computeFinalScore/showGameOverScreen가 호출되는지 검증.
 * - getState가 GAME_OVER를 반환하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito (GameModel 목킹).
 *
 * 주요 테스트 시나리오 예시:
 * - enter_invokesPauseAndComputeAndShow
 * - getState_returnsGameOver
 */
@ExtendWith(MockitoExtension.class)
class GameOverHandlerTest {

    @Mock GameModel model;
    GameOverHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GameOverHandler();
    }

    @Test
    void enter_invokesPauseAndComputeAndShow() {
        handler.enter(model);
        verify(model).pauseClock();
        verify(model).computeFinalScore();
        verify(model).showGameOverScreen();
    }

    @Test
    void getState_returnsGameOver() {
        assertEquals(GameState.GAME_OVER, handler.getState());
    }
}
