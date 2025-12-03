package tetris.multiplayer.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.GameModel;
import tetris.domain.GameModel.MultiplayerHook;
import tetris.domain.model.GameState;
import tetris.multiplayer.controller.LocalMultiPlayerController;
import tetris.multiplayer.model.MultiPlayerGame;
import tetris.multiplayer.model.PlayerState;

/*
 * 테스트 대상: tetris.multiplayer.handler.LocalMultiplayerHandler
 *
 * 역할 요약:
 * - 로컬 2P 대전을 위해 두 GameModel을 동기 업데이트하고, 멀티플레이 훅을 등록/해제한다.
 * - 게임 종료 시 두 플레이어와 상위 모델을 GAME_OVER로 전환하고 결과를 노출한다.
 *
 * 테스트 전략:
 * - enter 호출 시 ready 플래그를 false로 만들고 훅이 등록되는지 확인.
 * - game이 이미 종료된 상태에서 update를 호출하면 두 플레이어와 상위 모델을 GAME_OVER로 전환하는지 검증.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LocalMultiplayerHandlerTest {

    @Mock MultiPlayerGame game;
    @Mock LocalMultiPlayerController controller;
    @Mock GameModel hostModel;
    @Mock GameModel p1Model;
    @Mock GameModel p2Model;
    @Mock PlayerState p1State;
    @Mock PlayerState p2State;

    private LocalMultiplayerHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LocalMultiplayerHandler(game, controller, GameState.PLAYING);
        when(game.player(1)).thenReturn(p1State);
        when(game.player(2)).thenReturn(p2State);
        when(p1State.getModel()).thenReturn(p1Model);
        when(p2State.getModel()).thenReturn(p2Model);
        // controller hooks will call into game/controller; ensure no NPE
        doNothing().when(controller).onPieceLocked(eq(1), any(), any());
        doNothing().when(controller).onPieceLocked(eq(2), any(), any());
        doNothing().when(controller).injectAttackBeforeNextSpawn(eq(1));
        doNothing().when(controller).injectAttackBeforeNextSpawn(eq(2));
    }

    @Test
    void enter_resetsReadyFlags_andRegistersHooks() {
        handler.enter(hostModel);

        verify(p1State).setReady(false);
        verify(p2State).setReady(false);
        verify(p1Model).addMultiplayerHook(any(MultiplayerHook.class));
        verify(p2Model).addMultiplayerHook(any(MultiplayerHook.class));
    }

    @Test
    void update_whenGameAlreadyOver_transitionsPlayersAndHost() {
        when(game.isGameOver()).thenReturn(true);
        when(game.getWinnerId()).thenReturn(2);
        when(hostModel.getCurrentState()).thenReturn(GameState.PLAYING);

        handler.update(hostModel);

        verify(controller).withPlayer(eq(1), any());
        verify(controller).withPlayer(eq(2), any());
        verify(hostModel).changeState(GameState.GAME_OVER);
        verify(hostModel).showLocalMultiplayerResult(2);
        // game over already true, so markLoser should not be called
        verify(game, never()).markLoser(anyInt());
    }
}
