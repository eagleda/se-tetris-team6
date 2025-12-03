/**
 * 대상: tetris.multiplayer.controller.LocalMultiPlayerController
 *
 * 목적:
 * - pendingAttackLines/getPendingLines 스모크로 로컬 멀티 컨트롤러 커버리지를 보강한다.
 */
package tetris.multiplayer.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.multiplayer.model.MultiPlayerGame;

class LocalMultiPlayerControllerSmokeTest {

    @Test
    void pendingLines_and_attackLines_noThrow() {
        MultiPlayerGame game = Mockito.mock(MultiPlayerGame.class, Mockito.withSettings().lenient());
        LocalMultiPlayerController controller = new LocalMultiPlayerController(game);
        assertNotNull(controller.getPendingAttackLines(1));
        controller.injectAttackBeforeNextSpawn(1);
    }
}
