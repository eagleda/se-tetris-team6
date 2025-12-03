package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.view.GameComponent.MultiGameLayout
 *
 * 역할 요약:
 * - 로컬 멀티플레이 UI 레이아웃을 구성하는 패널.
 *
 * 테스트 전략:
 * - 생성 시 주요 서브패널(게임패널/공격큐/스코어/타이머)이 초기화되는지 리플렉션으로 확인.
 * - 타이머 가시성 토글 메서드가 예외 없이 실행되는지 확인.
 */
class MultiGameLayoutTest {

    @Test
    void initializesSubPanels() throws Exception {
        MultiGameLayout layout = new MultiGameLayout();
        assertTrue(layout.isVisible());
        assertFieldNotNull(layout, "gamePanel_1");
        assertFieldNotNull(layout, "gamePanel_2");
        assertFieldNotNull(layout, "attackQueuePanel_1");
        assertFieldNotNull(layout, "attackQueuePanel_2");
        assertFieldNotNull(layout, "timerPanel");

        layout.showTimer();
        layout.hideTimer();
    }

    private void assertFieldNotNull(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object val = f.get(target);
        assertTrue(val != null, name + " should be initialized");
    }
}
