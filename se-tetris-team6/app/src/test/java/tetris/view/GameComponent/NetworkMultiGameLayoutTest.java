package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.view.GameComponent.NetworkMultiGameLayout
 *
 * 역할 요약:
 * - 온라인 멀티플레이 UI 레이아웃을 구성하고 주기적으로 repaint 타이머를 운용한다.
 *
 * 테스트 전략:
 * - 생성 시 주요 서브패널과 repaint 타이머가 초기화되는지 리플렉션으로 확인.
 */
class NetworkMultiGameLayoutTest {

    @Test
    void initializesSubPanelsAndTimer() throws Exception {
        NetworkMultiGameLayout layout = new NetworkMultiGameLayout();
        assertTrue(layout.isVisible());
        assertFieldNotNull(layout, "gamePanel_1");
        assertFieldNotNull(layout, "gamePanel_2");
        assertFieldNotNull(layout, "attackQueuePanel_1");
        assertFieldNotNull(layout, "attackQueuePanel_2");
        assertFieldNotNull(layout, "timerPanel");
        assertFieldNotNull(layout, "repaintTimer");
    }

    private void assertFieldNotNull(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object val = f.get(target);
        assertTrue(val != null, name + " should be initialized");
    }
}
