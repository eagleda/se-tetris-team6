/*
 * 테스트 대상: tetris.multiplayer.controller.NetworkMultiPlayerController
 *
 * 역할 요약:
 * - 멀티플레이 게임 모델을 업데이트하고 네트워크 전송을 보조하는 컨트롤러.
 *
 * 테스트 전략:
 * - withLocalPlayer, injectAttackBeforeNextSpawn, sendGameState를 lenient mock으로 스모크 호출합니다.
 * - 네트워크/스레드 없이 예외 없이 통과하는지만 확인합니다.
 */
package tetris.multiplayer.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.multiplayer.model.MultiPlayerGame;

class NetworkMultiPlayerControllerCoreTest {

    @Test
    void coreMethods_executeWithoutException() {
        MultiPlayerGame game = mock(MultiPlayerGame.class, Mockito.withSettings().lenient());
        GameModel p1 = mock(GameModel.class, Mockito.withSettings().lenient());
        when(game.modelOf(1)).thenReturn(p1);
        when(game.getPendingAttackLines(anyInt())).thenReturn(Collections.emptyList());
        NetworkMultiPlayerController controller = new NetworkMultiPlayerController(game, 1);

        assertDoesNotThrow(() -> controller.withLocalPlayer(m -> {}));
        assertDoesNotThrow(() -> controller.injectAttackBeforeNextSpawn(1));
        assertDoesNotThrow(() -> controller.sendGameState(p1));
    }
}
