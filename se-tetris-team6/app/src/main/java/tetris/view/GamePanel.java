package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements KeyListener {
    public GamePanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.red);
        this.setOpaque(true);
        this.setVisible(false);

        JLabel label = new JLabel("game panel");
        this.add(label, BorderLayout.CENTER);

    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
