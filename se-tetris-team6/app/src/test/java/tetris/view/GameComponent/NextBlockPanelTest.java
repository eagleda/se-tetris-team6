package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.GameModel;
import tetris.domain.BlockKind;

/*
 * 테스트 대상: tetris.view.GameComponent.NextBlockPanel
 *
 * 역할 요약:
 * - 다음 블록 미리보기와 아이템 배지를 그리는 패널.
 *
 * 테스트 전략:
 * - GameModel mock을 바인딩한 뒤 paint 수행 시 예외 없이 동작하는지 확인.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NextBlockPanelTest {

    @Mock GameModel model;

    @Test
    void paintWithMockModel_doesNotThrow() {
        when(model.getNextBlockKind()).thenReturn(BlockKind.I);
        when(model.isItemMode()).thenReturn(false);
        when(model.isNextBlockItem()).thenReturn(false);
        when(model.isColorBlindMode()).thenReturn(false);

        NextBlockPanel panel = new NextBlockPanel();
        panel.setSize(120, 120);
        panel.bindGameModel(model);

        BufferedImage img = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
        panel.paint(img.getGraphics());
        assertNotNull(panel);
    }
}
