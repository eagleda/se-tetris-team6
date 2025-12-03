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
 * 테스트 대상: tetris.domain.handler.GamePlayHandler
 *
 * 역할 요약:
 * - PLAYING 상태에서 게임 루프를 담당하며, 진입 시 스폰/시계 재개, 업데이트 시 stepGameplay, 종료 시 시계 정지를 수행한다.
 *
 * 테스트 전략:
 * - enter/update/exit가 GameModel의 대응 메서드를 호출하는지 검증.
 * - getState가 PLAYING을 반환하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - enter_callsSpawnIfNeededAndResumeClock
 * - update_callsStepGameplay
 * - exit_callsPauseClock
 */
@ExtendWith(MockitoExtension.class)
class GamePlayHandlerTest {

    @Mock GameModel model;
    GamePlayHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GamePlayHandler();
    }

    @Test
    void enter_callsSpawnIfNeededAndResumeClock() {
        handler.enter(model);
        verify(model).spawnIfNeeded();
        verify(model).resumeClock();
    }

    @Test
    void update_callsStepGameplay() {
        handler.update(model);
        verify(model).stepGameplay();
    }

    @Test
    void exit_callsPauseClock() {
        handler.exit(model);
        verify(model).pauseClock();
    }

    @Test
    void getState_returnsPlaying() {
        assertEquals(GameState.PLAYING, handler.getState());
    }
}
