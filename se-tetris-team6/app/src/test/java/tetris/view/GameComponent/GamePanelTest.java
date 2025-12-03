package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.Board;
import tetris.domain.GameModel;

/*
 * 테스트 대상: tetris.view.GameComponent.GamePanel
 *
 * 역할 요약:
 * - 보드/활성 블록/그리드를 그리는 메인 플레이 필드 패널.
 *
 * 테스트 전략:
 * - GameModel을 mock으로 바인딩하고 paintComponent 호출 시 예외 없이 배경색이 유지되는지 확인.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GamePanelTest {

    @Mock GameModel model;
    @Mock Board board;

    @Test
    void paintComponent_withMockModel_doesNotThrowAndKeepsBackground() {
        GamePanel panel = new GamePanel();
        panel.setSize(200, 400);

        // 최소한의 스텁
        when(model.getBoard()).thenReturn(board);
        when(board.gridView()).thenReturn(new int[Board.H][Board.W]);
        when(model.getActiveBlock()).thenReturn(null);
        when(model.getActiveItemInfo()).thenReturn(null);

        panel.bindGameModel(model);

        BufferedImage img = new BufferedImage(200, 400, BufferedImage.TYPE_INT_ARGB);
        panel.paint(img.getGraphics());

        assertEquals(new java.awt.Color(18, 18, 18), panel.getBackground());
    }
}
