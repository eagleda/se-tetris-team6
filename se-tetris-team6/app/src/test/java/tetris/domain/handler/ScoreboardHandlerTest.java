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
 * 테스트 대상: tetris.domain.handler.ScoreboardHandler
 *
 * 역할 요약:
 * - SCOREBOARD 상태 진입 시 리더보드 데이터를 로드하도록 GameModel에 위임한다.
 *
 * 테스트 전략:
 * - enter가 loadScoreboard를 호출하는지 검증.
 * - getState가 SCOREBOARD를 반환하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - enter_callsLoadScoreboard
 * - getState_returnsScoreboard
 */
@ExtendWith(MockitoExtension.class)
class ScoreboardHandlerTest {

    @Mock GameModel model;
    ScoreboardHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ScoreboardHandler();
    }

    @Test
    void enter_callsLoadScoreboard() {
        handler.enter(model);
        verify(model).loadScoreboard();
    }

    @Test
    void getState_returnsScoreboard() {
        assertEquals(GameState.SCOREBOARD, handler.getState());
    }
}
