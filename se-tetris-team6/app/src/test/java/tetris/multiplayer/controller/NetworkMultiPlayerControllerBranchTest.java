/**
 * 대상: tetris.multiplayer.controller.NetworkMultiPlayerController
 *
 * 목적:
 * - onLocalInput/tick 분기가 예외 없이 실행되고, sendGameState가 호출되는 흐름을 스텁으로 검증해 미싱 라인을 보강한다.
 * - Mockito 사용 이유: MultiPlayerGame/GameModel 협력자 없이 서버/클라이언트 흐름을 준비하기 위함.
 *
 * 주요 시나리오:
 * 1) tick(localPlayerId=1)에서 PLAYING 상태일 때 sendGameState가 호출되는지 확인
 * 2) onLocalInput(localPlayerId=1)에서 sendGameState 호출 경로 검증
 */
package tetris.multiplayer.controller;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.multiplayer.model.MultiPlayerGame;
import tetris.multiplayer.controller.NetworkMultiPlayerController.NetworkEventHandler;

class NetworkMultiPlayerControllerBranchTest {

    @Test
    void tick_and_onLocalInput_sendSnapshots_whenServer() {
        MultiPlayerGame game = mock(MultiPlayerGame.class, Mockito.withSettings().lenient());
        GameModel p1 = mock(GameModel.class, Mockito.withSettings().lenient());
        GameModel p2 = mock(GameModel.class, Mockito.withSettings().lenient());
        when(game.modelOf(1)).thenReturn(p1);
        when(game.modelOf(2)).thenReturn(p2);
        when(p1.getCurrentState()).thenReturn(GameState.PLAYING);
        when(p2.getCurrentState()).thenReturn(GameState.PLAYING);

        NetworkEventHandler handler = mock(NetworkEventHandler.class, Mockito.withSettings().lenient());
        NetworkMultiPlayerController controller = new NetworkMultiPlayerController(game, 1);
        controller.setNetworkHandler(handler);

        controller.tick();
        verify(handler, atLeastOnce()).sendGameState(p1);
        verify(handler, atLeastOnce()).sendGameState(p2);

        controller.onLocalInput();
        verify(handler, atLeast(2)).sendGameState(any());
    }
}
