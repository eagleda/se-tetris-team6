package tetris.view;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.List;

import tetris.domain.leaderboard.LeaderboardEntry;
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

    /** Render top leaderboard entries (name: points) */
    public void renderLeaderboard(List<LeaderboardEntry> top) {
        if (top == null || top.isEmpty()) {
            scoreLabel.setText("No leaderboard entries yet.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (LeaderboardEntry e : top) {
            sb.append(String.format("%d. %s - %d    ", i++, e.getName(), e.getPoints()));
        }
        scoreLabel.setText(sb.toString());
    }
}
