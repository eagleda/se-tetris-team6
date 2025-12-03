package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.lang.reflect.Field;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.view.NetworkStatusOverlay
 *
 * 역할 요약:
 * - 네트워크 핑 상태를 색상 인디케이터와 텍스트로 표시하는 오버레이 패널.
 *
 * 테스트 전략:
 * - updateStatus 호출 시 상태 색상/텍스트가 핑 구간에 따라 적절히 변경되는지 검증.
 * - Swing invokeLater를 보장하기 위해 invokeAndWait로 동기화.
 */
class NetworkStatusOverlayTest {

    @Test
    void updateStatus_setsIndicatorColorAndText() throws Exception {
        NetworkStatusOverlay overlay = new NetworkStatusOverlay();
        JLabel indicator = getLabel(overlay, "statusIndicator");
        JLabel pingLabel = getLabel(overlay, "pingLabel");

        // disconnected
        overlay.updateStatus(-1);
        flushEdt();
        assertEquals(new Color(128, 128, 128), indicator.getForeground());
        assertEquals("-- ms", pingLabel.getText());

        // good
        overlay.updateStatus(50);
        flushEdt();
        assertEquals(new Color(0, 255, 0), indicator.getForeground());
        assertEquals("50 ms", pingLabel.getText());

        // moderate
        overlay.updateStatus(200);
        flushEdt();
        assertEquals(new Color(255, 255, 0), indicator.getForeground());
        assertEquals("200 ms", pingLabel.getText());

        // bad
        overlay.updateStatus(400);
        flushEdt();
        assertEquals(new Color(255, 0, 0), indicator.getForeground());
        assertEquals("400 ms", pingLabel.getText());
    }

    private JLabel getLabel(NetworkStatusOverlay overlay, String fieldName) throws Exception {
        Field f = NetworkStatusOverlay.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (JLabel) f.get(overlay);
    }

    private void flushEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> { /* sync */ });
    }
}
