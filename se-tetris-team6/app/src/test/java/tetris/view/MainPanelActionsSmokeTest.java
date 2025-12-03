/*
 * 테스트 대상: tetris.view.MainPanel$2, $3 (메뉴 버튼 액션 리스너)
 *
 * 역할 요약:
 * - 메인 메뉴에서 설정/스코어보드/종료 버튼을 눌렀을 때 상위 계층 훅을 호출합니다.
 * - 기본 구현은 비어 있으므로, 서브클래스로 플래그를 세팅해 호출 여부를 검증합니다.
 *
 * 테스트 전략:
 * - MainPanel을 익명 서브클래스로 생성하여 onScoreboardMenuClicked/onExitMenuClicked를 오버라이드하고,
 *   해당 버튼의 ActionListener를 직접 호출해 플래그가 설정되는지 확인합니다.
 *
 * 주요 테스트 시나리오 예시:
 * - 스코어보드 버튼 → onScoreboardMenuClicked 호출
 * - 종료 버튼 → onExitMenuClicked 호출
 */

package tetris.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JButton;

import org.junit.jupiter.api.Test;

class MainPanelActionsSmokeTest {

    @SuppressWarnings("unchecked")
    @Test
    void scoreboardAndExitButtons_invokeHooks() throws Exception {
        class HookPanel extends MainPanel {
            boolean scoreboardCalled = false;
            boolean exitCalled = false;

            @Override
            protected void onScoreboardMenuClicked() {
                scoreboardCalled = true;
            }

            @Override
            protected void onExitMenuClicked() {
                exitCalled = true;
            }
        }

        HookPanel panel = new HookPanel();

        Field buttonsField = MainPanel.class.getDeclaredField("buttons");
        buttonsField.setAccessible(true);
        List<JButton> buttons = (List<JButton>) buttonsField.get(panel);

        JButton scoreboardButton = buttons.get(3); // 순서: Single, Multi, Setting, Scoreboard, Exit
        JButton exitButton = buttons.get(4);

        // 직접 액션 호출
        scoreboardButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, "scoreboard"));
        exitButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, "exit"));

        assertTrue(panel.scoreboardCalled);
        assertTrue(panel.exitCalled);
    }
}
