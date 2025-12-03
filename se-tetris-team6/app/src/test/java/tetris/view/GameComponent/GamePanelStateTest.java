/**
 * 대상: tetris.view.GameComponent.GamePanel
 *
 * 목적:
 * - GameModel 바인딩 후 paint 호출이 예외 없이 수행되는지 스모크한다.
 */
package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.GameModel;

class GamePanelStateTest {

    @Test
    void bindModel_thenPaint_noThrow() {
        GamePanel panel = new GamePanel();
        GameModel model = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        panel.bindGameModel(model);
        assertDoesNotThrow(() -> panel.paint(panel.getGraphics()));
    }
}
