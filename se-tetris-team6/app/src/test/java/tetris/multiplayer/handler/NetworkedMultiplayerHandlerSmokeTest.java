/*
 * 테스트 대상: tetris.multiplayer.handler.NetworkedMultiplayerHandler
 *
 * 역할 요약:
 * - 네트워크 멀티플레이 서버/클라이언트 흐름에서 게임 상태를 브로드캐스트하고 종료를 판단합니다.
 *
 * 테스트 전략:
 * - MultiPlayerGame/Controller/GameModel을 lenient mock으로 구성해 enter/update/exit를 호출하며
 *   예외 없이 통과하는지 확인합니다.
 */
package tetris.multiplayer.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.multiplayer.controller.NetworkMultiPlayerController;
import tetris.multiplayer.model.MultiPlayerGame;

class NetworkedMultiplayerHandlerSmokeTest {

    @Test
    void enterUpdateExit_smoke() {
        MultiPlayerGame game = mock(MultiPlayerGame.class, Mockito.withSettings().lenient());
        NetworkMultiPlayerController controller = mock(NetworkMultiPlayerController.class, Mockito.withSettings().lenient());
        GameModel p1 = mock(GameModel.class, Mockito.withSettings().lenient());
        GameModel p2 = mock(GameModel.class, Mockito.withSettings().lenient());
        tetris.multiplayer.model.PlayerState ps1 = mock(tetris.multiplayer.model.PlayerState.class, Mockito.withSettings().lenient());
        tetris.multiplayer.model.PlayerState ps2 = mock(tetris.multiplayer.model.PlayerState.class, Mockito.withSettings().lenient());

        when(game.player(1)).thenReturn(ps1);
        when(game.player(2)).thenReturn(ps2);
        when(ps1.getModel()).thenReturn(p1);
        when(ps2.getModel()).thenReturn(p2);
        when(game.modelOf(1)).thenReturn(p1);
        when(game.modelOf(2)).thenReturn(p2);
        when(p1.getCurrentState()).thenReturn(GameState.PLAYING);
        when(p2.getCurrentState()).thenReturn(GameState.PLAYING);
        when(game.isGameOver()).thenReturn(false);

        NetworkedMultiplayerHandler handler = new NetworkedMultiplayerHandler(game, controller, GameState.PLAYING, 1, () -> {});
        GameModel model = mock(GameModel.class, Mockito.withSettings().lenient());

        assertDoesNotThrow(() -> handler.enter(model));
        assertDoesNotThrow(() -> handler.update(model));
        assertDoesNotThrow(() -> handler.exit(model));
    }
}
