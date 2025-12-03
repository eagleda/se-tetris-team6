/**
 * 대상: tetris.multiplayer.controller.NetworkMultiPlayerController
 *
 * 목적:
 * - withPlayer/withLocalPlayer, onLocalPieceLocked, injectAttackBeforeNextSpawn 등의 분기를 스모크해
 *   30%대 커버리지 미싱 라인을 줄인다.
 *
 * 주요 시나리오:
 * 1) withPlayer/withLocalPlayer가 올바른 플레이어에만 적용되는지 호출 시 예외 없이 실행
 * 2) onLocalPieceLocked 호출 시 내부 로직이 예외 없이 동작
 * 3) injectAttackBeforeNextSpawn에 빈 리스트 전달 시 예외 없이 반환
 */
package tetris.multiplayer.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.multiplayer.model.MultiPlayerGame;
import tetris.network.protocol.PlayerInput;

class NetworkMultiPlayerControllerBranchTest {

    private NetworkMultiPlayerController controller;
    private MultiPlayerGame game;

    @BeforeEach
    void setUp() {
        game = Mockito.mock(MultiPlayerGame.class, Mockito.withSettings().lenient());
        controller = new NetworkMultiPlayerController(game, 1);
        // 기본 모델 스텁
        tetris.domain.GameModel model = Mockito.mock(tetris.domain.GameModel.class, Mockito.withSettings().lenient());
        Mockito.when(model.getBoard()).thenReturn(new tetris.domain.Board());
        Mockito.when(game.modelOf(1)).thenReturn(model);
        Mockito.when(game.getPendingAttackLines(1)).thenReturn(java.util.Collections.emptyList());
    }

    @Test
    void withLocalPlayer_executeSafely() {
        assertDoesNotThrow(() -> controller.withLocalPlayer(g -> {}));
        assertDoesNotThrow(() -> controller.getPendingAttackLines(1));
    }

    @Test
    void onLocalPieceLocked_and_injectAttack_noThrow() {
        var snapshot = tetris.multiplayer.model.LockedPieceSnapshot.of(java.util.Collections.emptyList());
        assertDoesNotThrow(() -> controller.onLocalPieceLocked(snapshot, new int[] {0}));
        assertDoesNotThrow(() -> controller.injectAttackBeforeNextSpawn(1));
    }

    @Test
    void sendInput_noThrow() {
        assertDoesNotThrow(() -> controller.sendPlayerInput(new PlayerInput(tetris.network.protocol.InputType.MOVE_LEFT)));
    }
}
