package tetris.multiplayer.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.multiplayer.controller.NetworkMultiPlayerController;
import tetris.multiplayer.model.MultiPlayerGame;
import tetris.multiplayer.model.PlayerState;

/*
 * 테스트 대상: tetris.multiplayer.handler.NetworkedMultiplayerHandler
 *
 * 역할 요약:
 * - 네트워크 멀티플레이에서 로컬 플레이어만 틱 처리/스냅샷 전송을 담당하고,
 *   게임 종료 시 결과를 표시하고 GAME_END 콜백을 실행한다.
 *
 * 테스트 전략:
 * - enter 시 ready 플래그가 false로 초기화되고 로컬 플레이어(서버) 쪽에만 훅이 등록되는지 확인.
 * - 서버(localPlayerId=1)에서 한 플레이어가 GAME_OVER가 되면 markLoser와 결과 표시/콜백이 실행되는지 검증.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NetworkedMultiplayerHandlerTest {

    @Mock MultiPlayerGame game;
    @Mock NetworkMultiPlayerController controller;
    @Mock GameModel hostModel;
    @Mock GameModel p1Model;
    @Mock GameModel p2Model;
    @Mock PlayerState p1State;
    @Mock PlayerState p2State;

    private AtomicBoolean gameEndSent;
    private NetworkedMultiplayerHandler handler;

    @BeforeEach
    void setUp() {
        gameEndSent = new AtomicBoolean(false);
        handler = new NetworkedMultiplayerHandler(game, controller, GameState.PLAYING, 1, () -> gameEndSent.set(true));

        when(game.player(1)).thenReturn(p1State);
        when(game.player(2)).thenReturn(p2State);
        when(p1State.getModel()).thenReturn(p1Model);
        when(p2State.getModel()).thenReturn(p2Model);
        when(game.modelOf(1)).thenReturn(p1Model);
        when(game.modelOf(2)).thenReturn(p2Model);

    }

    @Test
    void enter_registersHooksForLocalServerAndResetsReady() {
        handler.enter(hostModel);

        verify(p1State).setReady(false);
        verify(p2State).setReady(false);
        verify(p1Model).addMultiplayerHook(any());
        verify(p2Model).addMultiplayerHook(any());
    }

    @Test
    void update_serverMarksLoserWhenPlayerGameOverAndSendsResult() {
        // P1 server: P1 is GAME_OVER, P2 still playing
        when(p1Model.getCurrentState()).thenReturn(GameState.GAME_OVER);
        when(p2Model.getCurrentState()).thenReturn(GameState.PLAYING);
        when(game.getWinnerId()).thenReturn(2);
        when(game.getLoserId()).thenReturn(1);
        // markLoser should flip game.getWinnerId() usage; track via invocation
        doAnswer(inv -> null).when(game).markLoser(1);

        handler.update(hostModel);

        verify(game).markLoser(1);
        verify(hostModel).changeState(GameState.GAME_OVER);
        verify(hostModel).showMultiplayerResult(2, 1);
        verify(controller).sendGameOverEvent();
    }
}
