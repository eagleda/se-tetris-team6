package tetris.view;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SettingPanel extends JPanel {
    public SettingPanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.green);
        this.setOpaque(true);
        this.setVisible(false);

        this.add(new JLabel("setting panel"), CENTER_ALIGNMENT);
    }
}
