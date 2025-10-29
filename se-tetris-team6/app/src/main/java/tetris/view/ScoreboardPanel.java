package tetris.view;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import tetris.domain.score.Score;

public class ScoreboardPanel extends JPanel implements ScoreView {

    private final JLabel scoreLabel;

    public ScoreboardPanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.blue);
        this.setOpaque(true);
        this.setVisible(false);

        this.scoreLabel = new JLabel("Score: 0");
        this.add(scoreLabel, CENTER_ALIGNMENT);
    }

    @Override
    public void renderScore(Score score) {
        String text = String.format("Score: %d | Lines: %d | Level: %d",
            score.getPoints(), score.getClearedLines(), score.getLevel());
        scoreLabel.setText(text);
    }
}
