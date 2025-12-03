/*
 * 테스트 대상: tetris.view.SettingPanel$1 (키 캡처용 KeyEventDispatcher)
 *
 * 역할 요약:
 * - Capture 버튼을 눌렀을 때 KeyEventDispatcher를 등록하여 첫 KEY_PRESSED 입력을
 *   텍스트 필드에 기록하고, 버튼 상태를 원복합니다.
 *
 * 테스트 전략:
 * - Capture 버튼을 클릭해 디스패처를 등록한 뒤, KeyEvent를 수동으로 전달하여
 *   필드 값이 설정되고 버튼이 원복되는지 확인합니다.
 *
 * 주요 테스트 시나리오 예시:
 * - KEY_PRESSED 이벤트를 전달하면 텍스트 필드가 채워지고 캡처 버튼이 다시 활성화된다.
 */

package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class SettingPanelCaptureTest {

    @Test
    void captureKey_setsFieldAndRestoresButtonState() throws Exception {
        SettingPanel panel = new SettingPanel();

        // Capture 버튼 클릭 -> dispatcher 등록
        panel.captureMoveLeftButton.doClick();

        // 리플렉션으로 activeCaptureDispatcher 가져와 수동으로 이벤트 전달
        Field dispatcherField = SettingPanel.class.getDeclaredField("activeCaptureDispatcher");
        dispatcherField.setAccessible(true);
        Object dispatcher = dispatcherField.get(panel);
        assertTrue(dispatcher instanceof java.awt.KeyEventDispatcher);

        KeyEvent press = new KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A');
        ((java.awt.KeyEventDispatcher) dispatcher).dispatchKeyEvent(press);

        // 필드에 값이 채워지고 버튼 상태가 원복되었는지 확인
        assertEquals("A", panel.keyMoveLeftField.getText());
        assertEquals("Capture", panel.captureMoveLeftButton.getText());
        assertTrue(panel.captureMoveLeftButton.isEnabled());

        // 디스패처가 제거되었는지 확인
        assertTrue(dispatcherField.get(panel) == null
                || !KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .dispatchEvent(new KeyEvent(panel, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, 0, ' ')));
    }
}
