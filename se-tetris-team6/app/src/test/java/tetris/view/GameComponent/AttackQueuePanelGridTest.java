/**
 * 대상: tetris.view.GameComponent.AttackQueuePanel
 *
 * 목적:
 * - setGrid와 paint 경로를 스모크해 추가 미싱 라인을 커버한다.
 */
package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class AttackQueuePanelGridTest {

    @Test
    void setGrid_and_paint_executes() {
        AttackQueuePanel panel = new AttackQueuePanel();
        panel.setGrid(new int[][] { {1,0,1}, {0,1,0} });
        assertDoesNotThrow(() -> panel.paint(panel.getGraphics()));
    }
}
