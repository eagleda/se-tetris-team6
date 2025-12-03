package tetris.multiplayer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.domain.Board;
import tetris.multiplayer.model.MultiPlayerGame;

/*
 * 테스트 대상: tetris.multiplayer.controller.NetworkMultiPlayerController
 *
 * 역할 요약:
 * - 네트워크 대전에서 로컬 플레이어 이벤트를 VersusRules에 전달하고 스냅샷/공격 줄을 네트워크로 전달하는 컨트롤러.
 *
 * 테스트 전략:
 * - withLocalPlayer가 로컬 ID에 해당하는 모델에만 Consumer를 적용하는지 검증.
 * - onLocalPieceLocked가 game.onPieceLocked 및 sendPieceLockedEvent/sendGameState를 호출하는지 확인.
 * - injectAttackBeforeNextSpawn가 대기 줄 조회 후 sendGameState를 호출하는지 검증(빈 리스트이면 조기 종료).
 */
@ExtendWith(MockitoExtension.class)
class NetworkMultiPlayerControllerTest {

    @Mock MultiPlayerGame game;
    @Mock GameModel modelP1;
    @Mock GameModel modelP2;

    private NetworkMultiPlayerController controller;

    @BeforeEach
    void setUp() {
        when(game.modelOf(1)).thenReturn(modelP1);
        when(game.modelOf(2)).thenReturn(modelP2);
        when(modelP1.getBoard()).thenReturn(new Board());
        when(modelP2.getBoard()).thenReturn(new Board());
        controller = Mockito.spy(new NetworkMultiPlayerController(game, 1));
        doNothing().when(controller).sendPieceLockedEvent(any(), any());
        doNothing().when(controller).sendGameState(any());
        doNothing().when(controller).sendGameOverEvent();
    }

    @Test
    void withLocalPlayer_appliesOnlyToLocalId() {
        controller.withLocalPlayer(m -> m.pauseGame());
        verify(modelP1).pauseGame();
        verify(modelP2, times(0)).pauseGame();
    }

    @Test
    void onLocalPieceLocked_delegatesAndSends() {
        int[] cleared = { 0 };
        controller.onLocalPieceLocked(null, cleared);

        verify(game).onPieceLocked(1, null, cleared, tetris.domain.Board.W);
        verify(controller).sendPieceLockedEvent(null, cleared);
        verify(controller).sendGameState(modelP1);
    }

    @Test
    void injectAttackBeforeNextSpawn_emptyQueue_noSnapshotSend() {
        when(game.takeAttackLinesForNextSpawn(1)).thenReturn(Collections.emptyList());

        controller.injectAttackBeforeNextSpawn(1);

        verify(game).takeAttackLinesForNextSpawn(1);
        // no sendGameState because no attack lines
        verify(controller, times(0)).sendGameState(any());
    }
}
