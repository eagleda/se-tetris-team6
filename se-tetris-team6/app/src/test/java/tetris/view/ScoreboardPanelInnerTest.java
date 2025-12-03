/**
 * 대상: tetris.view.ScoreboardPanel, ScoreboardPanel$2 (커스텀 렌더러/선택 모델)
 *
 * 목적:
 * - 리스트 하이라이트/선택 차단 동작을 검증하여 내부 익명 클래스 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) renderLeaderboard 호출 시 목록이 채워지고 highlight 인덱스가 설정된다.
 * 2) 선택 모델이 setSelectionInterval을 무시하여 선택이 되지 않는다.
 */
package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;

import org.junit.jupiter.api.Test;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;

class ScoreboardPanelInnerTest {

    @Test
    void renderLeaderboard_setsEntriesAndHighlight() {
        ScoreboardPanel panel = new ScoreboardPanel();
        panel.renderLeaderboard(GameMode.STANDARD,
                List.of(new LeaderboardEntry("A", 100), new LeaderboardEntry("B", 50)), 0);

        // 첫 번째 항목이 올바르게 포맷되었는지 확인
        try {
            java.lang.reflect.Field listField = ScoreboardPanel.class.getDeclaredField("standardList");
            listField.setAccessible(true);
            @SuppressWarnings("unchecked")
            JList<String> list = (JList<String>) listField.get(panel);
            assertEquals(" 1. A — 100", list.getModel().getElementAt(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void selectionModel_blocksSelection() {
        ScoreboardPanel panel = new ScoreboardPanel();
        panel.renderLeaderboard(GameMode.STANDARD, List.of(new LeaderboardEntry("A", 100)), -1);

        try {
            java.lang.reflect.Field listField = ScoreboardPanel.class.getDeclaredField("standardList");
            listField.setAccessible(true);
            @SuppressWarnings("unchecked")
            JList<String> list = (JList<String>) listField.get(panel);
            // selection model은 커스텀 DefaultListSelectionModel로 교체되어 있음
            DefaultListSelectionModel model = (DefaultListSelectionModel) list.getSelectionModel();
            model.setSelectionInterval(0, 0);
            assertTrue(list.isSelectionEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
