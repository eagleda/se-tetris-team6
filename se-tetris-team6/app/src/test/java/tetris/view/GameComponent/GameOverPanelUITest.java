package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.score.Score;

/*
 * 테스트 대상: tetris.view.GameComponent.GameOverPanel
 *
 * 역할 요약:
 * - 게임 종료 후 점수/리더보드/입력 필드를 표시하는 오버레이.
 *
 * 테스트 전략:
 * - show 호출 시 점수 텍스트와 입력 필드 표시 여부를 확인.
 * - renderLeaderboard가 모델을 갱신하는지 리스트 모델 크기로 검증.
 */
class GameOverPanelUITest {

    @Test
    void show_setsTextsAndVisibility() throws Exception {
        GameOverPanel panel = new GameOverPanel();
        Score score = Score.of(100, 2, 3);

        panel.show(score, true);

        JTextField nameField = (JTextField) getField(panel, "nameField");
        JButton saveButton = (JButton) getField(panel, "saveButton");
        JButton skipButton = (JButton) getField(panel, "skipButton");

        assertTrue(panel.isVisible());
        assertTrue(nameField.isVisible());
        assertTrue(saveButton.isVisible());
        assertTrue(skipButton.isVisible());
    }

    @Test
    void renderLeaderboard_updatesListModel() throws Exception {
        GameOverPanel panel = new GameOverPanel();
        java.util.List<LeaderboardEntry> entries = java.util.List.of(
                new LeaderboardEntry("AAA", 100),
                new LeaderboardEntry("BBB", 200));

        panel.renderLeaderboard(GameMode.STANDARD, entries);

        javax.swing.DefaultListModel<?> model = (javax.swing.DefaultListModel<?>) getField(panel, "standardModel");
        assertEquals(2, model.size());
        assertEquals(" 1. AAA — 100", model.get(0));
    }

    private Object getField(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
}
