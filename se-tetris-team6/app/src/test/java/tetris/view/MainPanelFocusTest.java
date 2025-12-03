/**
 * 대상: tetris.view.MainPanel (focusButton/clickFocusButton), MainPanel$2/$3
 *
 * 목적:
 * - 포커스 이동 및 clickFocusButton 동작을 검증하고, 현재 포커스 버튼 doClick을 통해 리스너 호출이 일어나는지 확인하여 0% 구간을 보강한다.
 *
 * 주요 시나리오:
 * 1) focusButton 호출 시 현재 포커스 배경색이 변경되고 인덱스가 이동한다.
 * 2) clickFocusButton 호출 시 현재 포커스된 버튼의 액션 리스너가 실행되어 훅 플래그가 true로 설정된다.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JButton;

import org.junit.jupiter.api.Test;

class MainPanelFocusTest {

    @SuppressWarnings("unchecked")
    @Test
    void focusAndClick_movesIndexAndTriggersAction() throws Exception {
        class HookPanel extends MainPanel {
            boolean called = false;
            @Override protected void onScoreboardMenuClicked() { called = true; }
        }
        HookPanel panel = new HookPanel();
        Field buttonsField = MainPanel.class.getDeclaredField("buttons");
        buttonsField.setAccessible(true);
        List<JButton> buttons = (List<JButton>) buttonsField.get(panel);

        Color initial = buttons.get(0).getBackground();
        panel.focusButton(1);
        Color after = buttons.get(1).getBackground();
        assertTrue(!after.equals(initial), "background should change after focus move");

        // 현재 포커스 버튼 클릭 → 첫 이동 후 index=1, Scoreboard 버튼은 index=3이므로 이동
        panel.focusButton(2); // move focus to index 3 (Scoreboard)
        panel.clickFocusButton(); // triggers onScoreboardMenuClicked via doClick
        assertTrue(panel.called);
    }
}
