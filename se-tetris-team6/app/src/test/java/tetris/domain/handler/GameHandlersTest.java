package tetris.domain.handler;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.domain.GameModel;

/*
 * 테스트 대상: 주요 GameHandler 구현들 (GamePlayHandler, PausedHandler, GameOverHandler)
 *
 * 역할 요약:
 * - 상태별 enter/exit/update에서 GameModel에 올바른 메서드를 호출하는지 검증한다.
 *
 * 테스트 전략:
 * - Mock GameModel로 각 핸들러 메서드를 호출하고 위임된 메서드가 정확히 호출되었는지 확인.
 */
@ExtendWith(MockitoExtension.class)
class GameHandlersTest {

    @Mock GameModel model;

    @Test
    void gamePlayHandler_callsSpawnAndClockAndStep() {
        GamePlayHandler handler = new GamePlayHandler();
        handler.enter(model);
        verify(model).spawnIfNeeded();
        verify(model).resumeClock();

        handler.update(model);
        verify(model).stepGameplay();

        handler.exit(model);
        verify(model).pauseClock();
    }

    @Test
    void pausedHandler_pausesAndResumesClock() {
        PausedHandler handler = new PausedHandler();

        handler.enter(model);
        verify(model).pauseClock();
        verify(model).showPauseOverlay();

        handler.exit(model);
        verify(model).hidePauseOverlay();
        verify(model).resumeClock();
    }

    @Test
    void gameOverHandler_computesFinalScoreAndShowsScreen() {
        GameOverHandler handler = new GameOverHandler();

        handler.enter(model);
        verify(model).pauseClock();
        verify(model).computeFinalScore();
        verify(model).showGameOverScreen();
    }
}
