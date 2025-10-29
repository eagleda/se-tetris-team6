package tetris.view.GameComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class ScorePanel extends JPanel {
    public ScorePanel() {
        // 패널 레이아웃 및 외형 설정
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.BLACK);

        // 빨간 테두리: 패널이 할당된 영역을 감싸도록 표시
        setBorder(BorderFactory.createLineBorder(Color.RED, 3));

        // 중앙 텍스트
        JTextPane text = new JTextPane();
        text.setText("ScorePanel");
        text.setEditable(false);
        text.setOpaque(false); // 배경은 패널 배경을 사용
        text.setForeground(Color.WHITE);
        text.setFont(new Font("SansSerif", Font.BOLD, 16));
        text.setFocusable(false);

        add(text, BorderLayout.CENTER);
    }
}
