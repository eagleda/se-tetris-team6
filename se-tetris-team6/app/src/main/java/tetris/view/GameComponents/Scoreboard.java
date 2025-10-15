package tetris.view.GameComponents;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Scoreboard extends JPanel {
    private final JLabel scoreLabel;

    public Scoreboard() {
        this.setPreferredSize(new Dimension(150, 60));
        this.setLayout(new GridBagLayout()); // 중앙 정렬
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        this.add(scoreLabel);
    }

    public void updateScore(int score) {
        // EDT에서 안전하게 UI 업데이트
        SwingUtilities.invokeLater(() -> scoreLabel.setText("Score: " + score));
    }
}