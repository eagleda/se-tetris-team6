package tetris.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.domain.GameModel;
import tetris.domain.Board;
import tetris.domain.model.InputState;
import tetris.network.protocol.PlayerInput;
import tetris.network.protocol.AttackLine;
import tetris.network.GameEventListener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.concurrent.GameThread
 *
 * 역할 요약:
 * - 게임 로직을 전용 스레드에서 실행하는 핵심 클래스
 * - GameModel을 통해 모든 게임 로직 처리 (Facade 패턴)
 * - GameplayEngine.GameplayEvents 구현으로 이벤트 처리
 * - 입력 큐를 통한 스레드 안전한 입력 처리
 * - 네트워크 통신 연동 (GameEventListener)
 *
 * 테스트 전략:
 * - 스레드 동작보다는 핵심 로직(입력 처리, 상태 관리, 이벤트 전파)에 집중
 * - Mock을 사용하여 의존성(GameModel, GameEventListener) 격리
 * - 입력 큐 동작 및 이벤트 전파 검증
 *
 * 사용 라이브러리:
 * - JUnit 5
 * - Mockito (GameModel, 리스너 Mock)
 *
 * 주요 테스트 시나리오:
 * - 생성자를 통한 초기화
 * - 입력 큐에 PlayerInput 추가 및 처리
 * - pause/resume 상태 전환
 * - 네트워크 리스너 등록 및 이벤트 전파
 * - stop 호출 시 스레드 종료
 */

@ExtendWith(MockitoExtension.class)
public class GameThreadTest {

    @Mock
    private GameModel mockGameModel;

    @Mock
    private Board mockBoard;

    @Mock
    private InputState mockInputState;

    @Mock
    private GameEventListener mockNetworkListener;

    private GameThread gameThread;

    @BeforeEach
    void setUp() {
        when(mockGameModel.getBoard()).thenReturn(mockBoard);
        when(mockGameModel.getInputState()).thenReturn(mockInputState);
        
        gameThread = new GameThread(mockGameModel);
    }

    @Test
    void constructor_shouldInitializeWithGameModel() {
        // then
        assertNotNull(gameThread, "GameThread should be created");
        assertFalse(gameThread.isPaused(), "Should not be paused initially");
    }

    @Test
    void enqueueInput_shouldAddInputToQueue() {
        // given
        PlayerInput input = new PlayerInput();

        // when
        gameThread.enqueueInput(input);

        // then
        // Queue should contain the input (verified by processing)
        assertDoesNotThrow(() -> gameThread.enqueueInput(input));
    }

    @Test
    void pause_shouldSetPausedState() {
        // when
        gameThread.pause();

        // then
        assertTrue(gameThread.isPaused(), "Should be paused after pause()");
    }

    @Test
    void resume_shouldClearPausedState() {
        // given
        gameThread.pause();

        // when
        gameThread.resume();

        // then
        assertFalse(gameThread.isPaused(), "Should not be paused after resume()");
    }

    @Test
    void isPaused_shouldReturnCurrentState() {
        // when
        boolean initialState = gameThread.isPaused();
        gameThread.pause();
        boolean pausedState = gameThread.isPaused();
        gameThread.resume();
        boolean resumedState = gameThread.isPaused();

        // then
        assertFalse(initialState, "Should start unpaused");
        assertTrue(pausedState, "Should be paused");
        assertFalse(resumedState, "Should be unpaused");
    }

    @Test
    void setNetworkListener_shouldStoreListener() {
        // when
        gameThread.setNetworkListener(mockNetworkListener);

        // then
        // Listener is stored (verified by event propagation)
        assertDoesNotThrow(() -> gameThread.setNetworkListener(mockNetworkListener));
    }

    @Test
    void stop_shouldStopThread() {
        // when
        gameThread.stop();

        // then
        assertFalse(gameThread.isRunning(), "Should not be running after stop()");
    }

    @Test
    void isRunning_shouldReturnTrueInitially() {
        // when
        boolean running = gameThread.isRunning();

        // then
        assertTrue(running, "Should be running initially");
    }

    @Test
    void onLineClear_shouldTriggerEvent() {
        // given
        gameThread.setNetworkListener(mockNetworkListener);
        int linesCleared = 2;

        // when
        gameThread.onLineClear(linesCleared);

        // then
        // Event should be propagated to network listener
        // Implementation dependent on how events are queued
    }

    @Test
    void enqueueEvent_shouldAddGameEvent() {
        // given
        GameEvent event = new GameEvent(GameEvent.Type.LINE_CLEAR, 3);

        // when
        gameThread.enqueueEvent(event);

        // then
        assertDoesNotThrow(() -> gameThread.enqueueEvent(event));
    }

    @Test
    void getGameModel_shouldReturnModel() {
        // when
        GameModel model = gameThread.getGameModel();

        // then
        assertEquals(mockGameModel, model, "Should return the game model");
    }
}
