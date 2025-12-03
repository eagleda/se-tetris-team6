package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.BlockKind;
import tetris.domain.GameModel;
import tetris.domain.GameModel.ActiveItemInfo;
import tetris.domain.block.BlockLike;
import tetris.domain.item.ItemType;
import tetris.domain.model.Block;
import tetris.domain.Board;

/**
 * GamePanel의 아이템 하이라이트 오버레이 분기를 추가로 커버한다.
 * - ActiveItemInfo가 존재하고 아이템 셀 좌표가 주어진 경우 paintComponent가 예외 없이 실행되는지 확인.
 */
class GamePanelItemOverlayTest {

    @Test
    void paint_withActiveItemOverlay_doesNotThrow() {
        GameModel model = mock(GameModel.class, Mockito.withSettings().lenient());
        Board board = new Board();
        when(model.getBoard()).thenReturn(board);

        Block active = Block.spawn(BlockKind.I, 3, 5);
        when(model.getActiveBlock()).thenReturn(active);

        // 아이템 셀 좌표 (0,0) 라벨 double_score
        BlockLike delegate = active;
        ActiveItemInfo info = new ActiveItemInfo(delegate, "double_score", ItemType.ACTIVE, 0, 0);
        when(model.getActiveItemInfo()).thenReturn(info);

        GamePanel panel = new GamePanel();
        panel.bindGameModel(model);
        panel.setSize(300, 600);

        BufferedImage img = new BufferedImage(300, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        assertDoesNotThrow(() -> panel.paintComponent(g2));
        g2.dispose();
    }
}
