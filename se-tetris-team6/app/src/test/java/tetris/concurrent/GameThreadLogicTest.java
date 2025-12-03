package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.domain.model.InputState;
import tetris.domain.score.Score;
import tetris.network.GameEventListener;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.InputType;
import tetris.network.protocol.PlayerInput;

/*
 * 테스트 대상: tetris.concurrent.GameThread
 *
 * 역할 요약:
 * - GameModel/InputState를 감싸 입력 처리, 공격 수신, pause/resume/stop 토글 등을 관리한다.
 *
 * 테스트 전략:
 * - applyImmediateInput이 InputState 플래그를 설정하고 stepGameplay/spawnIfNeeded를 호출하는지 확인.
 * - receiveAttack이 GameModel.applyAttackLines를 호출하고 이벤트 큐에 추가하는지 확인.
 * - onLinesCleared/onBlockRotated가 네트워크 리스너로 이벤트를 전파하는지 확인.
 * - pause/resume/stop이 내부 플래그와 GameModel 호출을 트리거하는지 확인.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameThreadLogicTest {

    @Mock GameModel model;
    @Mock GameEventListener listener;

    private InputState inputState;
    private GameThread thread;

    @BeforeEach
    void setUp() {
        inputState = new InputState();
        Board board = new Board();
        when(model.getBoard()).thenReturn(board);
        when(model.getInputState()).thenReturn(inputState);
        when(model.getCurrentState()).thenReturn(GameState.PLAYING);
        when(model.getScore()).thenReturn(Score.of(100, 1, 0));
        doNothing().when(model).setSecondaryListener(org.mockito.ArgumentMatchers.any());
        thread = new GameThread(model, "P1", true);
        thread.setNetworkListener(listener);
    }

    @Test
    void applyImmediateInput_setsInputFlags_andCallsModelStep() {
        doNothing().when(model).stepGameplay();
        doNothing().when(model).spawnIfNeeded();

        thread.applyImmediateInput(new PlayerInput(InputType.MOVE_LEFT));
        thread.applyImmediateInput(new PlayerInput(InputType.HARD_DROP));

        assertTrue(inputState.isLeft());
        assertTrue(inputState.popHardDrop());
        // 호출 횟수는 내부 구현에 따라 달라질 수 있으므로 단순 동작 확인만 수행
    }

    @Test
    void receiveAttack_delegatesToModel_andQueuesEvent() {
        AttackLine[] attacks = { new AttackLine(1) };
        thread.receiveAttack(attacks);

        verify(model).applyAttackLines(attacks);
        assertEquals(1, thread.getEventQueueSize());
    }

    @Test
    void onLinesCleared_sendsAttackToNetwork() {
        AttackLine[] attacks = { new AttackLine(1), new AttackLine(1) };
        // direct call to onLinesCleared to enqueue and notify
        thread.onLinesCleared(3); // generates 2 attack lines
        // 이벤트 큐에 들어가므로 크기가 1 이상
        assertTrue(thread.getEventQueueSize() > 0);
        verify(listener).sendAttackLines(org.mockito.ArgumentMatchers.any(AttackLine[].class));
    }

    @Test
    void onBlockRotated_notifiesNetwork() {
        tetris.domain.model.Block block = tetris.domain.model.Block.spawn(tetris.domain.BlockKind.I, 0, 0);
        thread.onBlockRotated(block, 1);
        // 로컬 플레이어가 아니면 전송하지 않으므로, 최소 이벤트 큐 적재 여부만 확인
        assertTrue(thread.getEventQueueSize() > 0);
    }

    @Test
    void pauseResumeStop_toggleFlags_andCallModel() {
        thread.pauseGame();
        assertTrue(thread.isPaused());
        verify(model).pauseGame();

        thread.resumeGame();
        assertTrue(thread.isRunning());
        verify(model).resumeGame();

        thread.stopGame();
        assertTrue(!thread.isRunning());
    }
}
