package tetris.view.GameComponent;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.view.GameComponent.RemotePlayerPanel
 *
 * 역할 요약:
 * - GamePanel을 확장해 "OPP" 라벨을 그리는 원격 플레이어용 패널.
 *
 * 테스트 전략:
 * - 인스턴스화 후 paint 호출 시 예외가 발생하지 않는지 확인한다.
 */
class RemotePlayerPanelTest {

    @Test
    void paintDoesNotThrow() {
        RemotePlayerPanel panel = new RemotePlayerPanel();
        panel.setSize(100, 200);
        BufferedImage img = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
        panel.paint(img.getGraphics());
    }
}
