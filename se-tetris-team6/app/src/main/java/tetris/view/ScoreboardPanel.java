package tetris.view;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ScoreboardPanel extends JPanel {
    public ScoreboardPanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.blue);
        this.setOpaque(true);
        this.setVisible(false);

        this.add(new JLabel("scoreboard panel"), CENTER_ALIGNMENT);

    }
}
