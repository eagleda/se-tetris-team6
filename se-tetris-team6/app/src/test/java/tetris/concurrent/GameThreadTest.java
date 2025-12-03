package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.model.Block;
import tetris.domain.model.InputState;
import tetris.network.protocol.InputType;
import tetris.network.protocol.PlayerInput;

/*
 * 테스트 대상: tetris.concurrent.GameThread
 *
 * 역할 요약:
 * - GameModel과 연동해 게임 루프를 실행하고, 네트워크/외부로 이벤트를 전달하는 스레드.
 * - 입력 큐 처리, 일시정지/재개, 즉시 입력 반영(예측) 등을 담당한다.
 *
 * 테스트 전략:
 * - applyImmediateInput: InputState에 입력이 반영되고 GameModel.stepGameplay가 호출되는지 검증.
 * - pause/resume/stop: 내부 플래그와 GameModel 호출 여부를 확인.
 * - isRunning/isPaused 플래그는 리플렉션으로 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameThreadTest {

    @Mock GameModel gameModel;
    @Mock Block activeBlock;

    private InputState inputState;
    private GameThread gameThread;

    @BeforeEach
    void setUp() {
        inputState = new InputState();
        when(gameModel.getBoard()).thenReturn(new Board());
        when(gameModel.getInputState()).thenReturn(inputState);
        gameThread = new GameThread(gameModel, "P1", true);
    }

    @Test
    void applyImmediateInput_updatesInputState_andStepsGameplay() {
        gameThread.applyImmediateInput(new PlayerInput(InputType.MOVE_LEFT));

        assertTrue(inputState.isLeft());
        verify(gameModel).stepGameplay();
    }

    @Test
    void pauseAndResume_updateFlagsAndCallModel() throws Exception {
        gameThread.pauseGame();
        assertTrue(readAtomicBoolean(gameThread, "isPaused"));
        verify(gameModel).pauseGame();

        gameThread.resumeGame();
        assertFalse(readAtomicBoolean(gameThread, "isPaused"));
        verify(gameModel).resumeGame();
    }

    @Test
    void stopGame_setsRunningFalse() throws Exception {
        gameThread.stopGame();
        assertFalse(readAtomicBoolean(gameThread, "isRunning"));
    }

    private boolean readAtomicBoolean(GameThread target, String fieldName) throws Exception {
        Field f = GameThread.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        java.util.concurrent.atomic.AtomicBoolean val = (java.util.concurrent.atomic.AtomicBoolean) f.get(target);
        return val.get();
    }
}
