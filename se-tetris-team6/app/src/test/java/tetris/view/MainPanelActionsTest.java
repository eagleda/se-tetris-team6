/**
 * 대상: tetris.view.MainPanel (익명 리스너 $2, $3 포함)
 *
 * 목적:
 * - 단순 오버라이드 훅을 이용해 싱글/멀티 플레이 선택 시 호출되는 콜백이 실행되는지 스모크한다.
 *   (실제 다이얼로그를 띄우지 않고 보호된 메서드를 직접 호출)
 *
 * 주요 시나리오:
 * 1) onSinglePlayConfirmed 호출 시 플래그가 설정되는지 확인
 * 2) onMultiPlayConfirmed 호출 시 전달된 모드/온라인/서버 여부가 기록되는지 확인
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainPanelActionsTest {

    @Test
    void singleAndMultiHooks_areInvoked() {
        class TestPanel extends MainPanel {
            boolean singleCalled = false;
            String lastMode;
            boolean lastOnline;
            boolean lastServer;
            @Override
            protected void onSinglePlayConfirmed(String mode) {
                singleCalled = true;
                lastMode = mode;
            }
            @Override
            protected void onMultiPlayConfirmed(String mode, boolean isOnline, boolean isServer) {
                lastMode = mode;
                lastOnline = isOnline;
                lastServer = isServer;
            }
        }

        TestPanel panel = new TestPanel();
        panel.onSinglePlayConfirmed("NORMAL");
        assertTrue(panel.singleCalled);
        assertEquals("NORMAL", panel.lastMode);

        panel.onMultiPlayConfirmed("ITEM", true, false);
        assertEquals("ITEM", panel.lastMode);
        assertTrue(panel.lastOnline);
        assertEquals(false, panel.lastServer);
    }
}
