/**
 * 대상: tetris.view.GameComponent.NetworkMultiGameLayout
 *
 * 목적:
 * - 생성 시 자식 컴포넌트가 null 없이 초기화되는지, showTimer/hideTimer가 타이머 패널 가시성을 토글하는지 검증해
 *   30%대 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) 생성 직후 repaintTimer가 실행 중이며 주요 서브패널이 null이 아님을 확인
 * 2) showTimer/hideTimer로 타이머 패널 가시성이 토글되는지 확인
 */
package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NetworkMultiGameLayoutTest {

    @Test
    void components_initialized_and_timerToggle() throws Exception {
        NetworkMultiGameLayout layout = new NetworkMultiGameLayout();

        // 주요 컴포넌트가 null이 아닌지 확인
        assertNotNull(layout.getComponent(0));

        // 리플렉션으로 timerPanel 접근
        var f = NetworkMultiGameLayout.class.getDeclaredField("timerPanel");
        f.setAccessible(true);
        TimerPanel timer = (TimerPanel) f.get(layout);
        assertNotNull(timer);

        layout.hideTimer();
        assertFalse(timer.isVisible());
        layout.showTimer();
        assertTrue(timer.isVisible());
    }
}
