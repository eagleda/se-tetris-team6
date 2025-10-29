package tetris.view;

import java.awt.Color;
import javax.swing.JPanel;

import java.util.List;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.score.Score;

public class ScoreboardPanel extends JPanel implements ScoreView {

    private final javax.swing.DefaultListModel<String> listModel = new javax.swing.DefaultListModel<>();
    private final javax.swing.JList<String> list = new javax.swing.JList<>(listModel);

    public ScoreboardPanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.darkGray);
        this.setOpaque(true);
        this.setVisible(false);
        this.setLayout(new java.awt.BorderLayout());

        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(list);
        sp.setOpaque(false);
        this.add(sp, java.awt.BorderLayout.CENTER);
    }

    @Override
    public void renderScore(Score score) {
        // not used here; current score displayed in-game elsewhere
    }

    /** Render top leaderboard entries (name: points) */
    public void renderLeaderboard(List<LeaderboardEntry> top) {
        listModel.clear();
        if (top == null || top.isEmpty()) {
            listModel.addElement("No leaderboard entries yet.");
            return;
        }
        int i = 1;
        for (LeaderboardEntry e : top) {
            listModel.addElement(String.format("%2d. %s — %d", i++, e.getName(), e.getPoints()));
        }
    }
}
