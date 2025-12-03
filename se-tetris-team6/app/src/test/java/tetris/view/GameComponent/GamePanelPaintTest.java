/**
 * 대상: tetris.view.GameComponent.GamePanel
 *
 * 목적:
 * - paintComponent가 최소한의 모델 상태에서 예외 없이 동작하는지 확인하여
 *   30%대 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) 빈 보드/activeBlock=null 상태에서 paintComponent 호출 시 예외 없이 종료
 * 2) activeBlock이 있을 때도 예외 없이 그려지는지 스모크
 *
 * Mockito 사용 이유:
 * - GameModel/Board 협력자를 간단히 스텁해 렌더링 루프를 실행하기 위함.
 */
package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.Board;
import tetris.domain.BlockKind;
import tetris.domain.GameModel;
import tetris.domain.block.BlockLike;
import tetris.domain.model.Block;

class GamePanelPaintTest {

    @Test
    void paint_withEmptyBoard_doesNotThrow() {
        GameModel model = mock(GameModel.class, Mockito.withSettings().lenient());
        Board board = new Board();
        when(model.getBoard()).thenReturn(board);
        when(model.getActiveBlock()).thenReturn(null);
        when(model.getActiveItemInfo()).thenReturn(null);

        GamePanel panel = new GamePanel();
        panel.bindGameModel(model);
        panel.setSize(200, 400);

        BufferedImage img = new BufferedImage(200, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        assertDoesNotThrow(() -> panel.paintComponent(g2));
        g2.dispose();
    }

    @Test
    void paint_withActiveBlock_doesNotThrow() {
        GameModel model = mock(GameModel.class, Mockito.withSettings().lenient());
        Board board = new Board();
        when(model.getBoard()).thenReturn(board);
        Block active = Block.spawn(BlockKind.I, 0, 0);
        when(model.getActiveBlock()).thenReturn(active);
        when(model.getActiveItemInfo()).thenReturn(null);

        GamePanel panel = new GamePanel();
        panel.bindGameModel(model);
        panel.setSize(200, 400);

        BufferedImage img = new BufferedImage(200, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        assertDoesNotThrow(() -> panel.paintComponent(g2));
        g2.dispose();
    }
}
