package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PausePanel extends JPanel {
    public PausePanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(new Color(0, 0, 0, 150));
        this.setOpaque(false);
        this.setVisible(false);

        this.setLayout(new BorderLayout());

        JLabel label = new JLabel("pause panel");
        label.setForeground(Color.white);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        this.add(label, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}
