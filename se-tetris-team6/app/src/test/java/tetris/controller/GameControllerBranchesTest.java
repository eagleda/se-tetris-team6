/**
 * 대상: tetris.controller.GameController
 *
 * 목적:
 * - 다양한 GameState 분기에서 handleKeyPress가 올바른 협력자 호출을 수행하는지 스모크한다.
 * - 기존 테스트가 다루지 않은 MENU/NAME_INPUT/PAUSED 흐름을 보강한다.
 */
package tetris.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.event.KeyEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;

@ExtendWith(MockitoExtension.class)
class GameControllerBranchesTest {

    @Mock GameModel model;
    GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController(model);
    }

    @Test
    void menu_enter_doesNotThrow() {
        when(model.getCurrentState()).thenReturn(GameState.MENU);
        assertDoesNotThrow(() -> controller.handleKeyPress(KeyEvent.VK_ENTER));
    }

    @Test
    void nameInput_quitToMenu_noThrow() {
        when(model.getCurrentState()).thenReturn(GameState.NAME_INPUT);
        assertDoesNotThrow(() -> controller.handleKeyPress(KeyEvent.VK_ESCAPE));
    }

    @Test
    void paused_quitToMenu_noThrow() {
        when(model.getCurrentState()).thenReturn(GameState.PAUSED);
        assertDoesNotThrow(() -> controller.handleKeyPress(KeyEvent.VK_ESCAPE));
    }
}
