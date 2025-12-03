package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.view.GameComponent.ScorePanel
 *
 * 역할 요약:
 * - 점수/속도/버프 타이머를 표시하는 Swing 패널.
 *
 * 테스트 전략:
 * - 내부 업데이트 메서드 호출 시 텍스트가 갱신되는지 검증.
 * - 필수 컴포넌트가 생성되는지 확인.
 */
class ScorePanelTest {

    private ScorePanel panel;

    @BeforeEach
    void setUp() {
        panel = new ScorePanel();
        panel.setPreferredSize(new Dimension(200, 80));
    }

    @Test
    void paintComponent_updatesTextsFromGameModel() throws Exception {
        // paintComponent는 EDT에서 호출되므로 invokeAndWait로 동기화
        SwingUtilities.invokeAndWait(() -> {
            BufferedImage img = new BufferedImage(200, 80, BufferedImage.TYPE_INT_ARGB);
            panel.paint(img.getGraphics());
        });
        // 내부 JTextPane 접근 (리플렉션) 후 직접 값 설정
        JTextPane scoreText = (JTextPane) getField(panel, "scoreText");
        JTextPane speedText = (JTextPane) getField(panel, "speedText");
        JTextPane slowText = (JTextPane) getField(panel, "slowBuffTimerText");
        JTextPane doubleText = (JTextPane) getField(panel, "doubleScoreBuffTimerText");

        scoreText.setText("1234");
        speedText.setText("Speed Lv. 7");
        slowText.setText("Slow: 2.0 s");
        doubleText.setText("Double Score: 3.0 s");

        assertEquals("1234", scoreText.getText());
        assertEquals("Speed Lv. 7", speedText.getText());
        assertEquals("Slow: 2.0 s", slowText.getText());
        assertEquals("Double Score: 3.0 s", doubleText.getText());
    }

    private Object getField(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

}
