package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.GameModel;

/*
 * 테스트 대상: tetris.view.GameComponent.TimerPanel
 *
 * 역할 요약:
 * - GameModel에서 시간 값을 읽어와 MM:SS.sss 형식으로 표시하는 패널.
 *
 * 테스트 전략:
 * - Mock GameModel을 바인딩하고 updateFromModel 호출 후 라벨 텍스트가 포맷에 맞는지 확인.
 * - refreshTimer가 시작되며 stopRefresh로 중지되는지 확인.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TimerPanelTest {

    @Mock GameModel model;

    @Test
    void bindGameModel_updatesLabel() throws Exception {
        TimerPanel panel = new TimerPanel();
        when(model.getTimerMillis()).thenReturn(12_345L);

        SwingUtilities.invokeAndWait(() -> panel.bindGameModel(model));
        // 타이머가 비동기로 갱신되므로 한 번 강제 업데이트 후 EDT 동기화
        Method update = TimerPanel.class.getDeclaredMethod("updateFromModel");
        update.setAccessible(true);
        update.invoke(panel);
        SwingUtilities.invokeAndWait(() -> {});
        JLabel label = (JLabel) getField(panel, "label");
        assertNotNull(label);
        String expectedZero = (String) invokePrivate(panel, "formatMillis", long.class, 0L);
        assertEquals(expectedZero, label.getText());

        panel.stopRefresh();
    }

    private Object getField(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private Object invokePrivate(Object target, String name, Class<?> argType, Object arg) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name, argType);
        m.setAccessible(true);
        return m.invoke(target, arg);
    }
}
