package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class GamePanel extends JPanel {
    public GamePanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.red);
        this.setOpaque(true);
        this.setVisible(false);

        JLabel label = new JLabel("game panel");
        this.add(label, BorderLayout.CENTER);

    }
}
