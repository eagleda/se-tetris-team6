package tetris.view.GameComponent;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.view.GameComponent.LocalPlayerPanel
 *
 * 역할 요약:
 * - GamePanel을 확장해 "YOU" 라벨을 그리는 로컬 플레이어용 패널.
 *
 * 테스트 전략:
 * - 단순히 인스턴스화 후 paint 호출 시 예외가 발생하지 않는지 확인한다.
 */
class LocalPlayerPanelTest {

    @Test
    void paintDoesNotThrow() {
        LocalPlayerPanel panel = new LocalPlayerPanel();
        panel.setSize(100, 200);
        BufferedImage img = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
        panel.paint(img.getGraphics());
    }
}
